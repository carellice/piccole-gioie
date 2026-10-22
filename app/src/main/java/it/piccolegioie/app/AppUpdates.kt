package it.piccolegioie.app

import android.app.Application
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.content.FileProvider
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import java.security.MessageDigest
import kotlin.coroutines.coroutineContext

data class AppRelease(val versionCode: Long, val versionName: String, val apkUrl: String, val sha256: String, val size: Long)
enum class UpdatePhase { Idle, Checking, Current, Available, Downloading, Ready, Error }
data class UpdateState(val phase: UpdatePhase = UpdatePhase.Idle, val release: AppRelease? = null,
    val progress: Float = 0f, val message: String = "")
private class UpdateProblem(message: String) : Exception(message)

class AppUpdateViewModel(application: Application) : AndroidViewModel(application) {
    private val mutableState = MutableStateFlow(UpdateState())
    val state = mutableState.asStateFlow()
    private var downloadedApk: File? = null

    fun check() {
        if (state.value.phase in listOf(UpdatePhase.Checking, UpdatePhase.Downloading)) return
        viewModelScope.launch {
            mutableState.value = UpdateState(UpdatePhase.Checking)
            try {
                val release = withContext(Dispatchers.IO) { GitHubUpdates.latest() }
                mutableState.value = if (release != null && UpdatePolicy.isNewer(BuildConfig.VERSION_CODE.toLong(), release.versionCode))
                    UpdateState(UpdatePhase.Available, release) else UpdateState(UpdatePhase.Current)
            } catch (e: CancellationException) { throw e
            } catch (e: Exception) { fail(e) }
        }
    }

    fun download() {
        val release = state.value.release ?: return
        if (state.value.phase != UpdatePhase.Available) return
        viewModelScope.launch {
            mutableState.value = UpdateState(UpdatePhase.Downloading, release)
            try {
                downloadedApk = withContext(Dispatchers.IO) {
                    GitHubUpdates.download(getApplication(), release) { progress ->
                        mutableState.value = UpdateState(UpdatePhase.Downloading, release, progress)
                    }
                }
                mutableState.value = UpdateState(UpdatePhase.Ready, release)
            } catch (e: CancellationException) { throw e
            } catch (e: Exception) { fail(e) }
        }
    }

    fun install(context: Context) {
        val apk = downloadedApk ?: return
        val release = state.value.release ?: return
        try {
            if (!context.packageManager.canRequestPackageInstalls()) {
                context.startActivity(Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES, Uri.parse("package:${context.packageName}")))
                mutableState.value = state.value.copy(message = "Consenti gli aggiornamenti da piccole gioie, poi torna qui e tocca Installa.")
                return
            }
            GitHubUpdates.verifyPackage(context, apk, release)
            val uri = FileProvider.getUriForFile(context, "${context.packageName}.updates", apk)
            context.startActivity(Intent(Intent.ACTION_VIEW).setDataAndType(uri, "application/vnd.android.package-archive")
                .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION))
        } catch (e: Exception) { fail(e) }
    }

    private fun fail(error: Exception) {
        mutableState.value = UpdateState(UpdatePhase.Error, message = (error as? UpdateProblem)?.message
            ?: "Non riesco a contattare GitHub. Controlla la connessione e riprova tra poco.")
    }
}

private object GitHubUpdates {
    private const val API = "https://api.github.com/repos/${UpdatePolicy.REPOSITORY}/releases/latest"

    suspend fun latest(): AppRelease? {
        val releaseJson = json(API, allowMissing = true) ?: return null
        if (releaseJson.optBoolean("draft") || releaseJson.optBoolean("prerelease")) return null
        val assets = releaseJson.getJSONArray("assets")
        fun asset(name: String): JSONObject = (0 until assets.length()).map { assets.getJSONObject(it) }
            .firstOrNull { it.optString("name") == name && it.optString("state") == "uploaded" }
            ?: throw UpdateProblem("Questa release è ancora in preparazione. Riprova tra poco.")
        val metadataUrl = asset(UpdatePolicy.METADATA_NAME).getString("browser_download_url")
        val apk = asset(UpdatePolicy.APK_NAME)
        val apkUrl = apk.getString("browser_download_url")
        require(UpdatePolicy.isReleaseAsset(metadataUrl) && UpdatePolicy.isReleaseAsset(apkUrl))
        val metadata = requireNotNull(json(metadataUrl))
        val code = metadata.getLong("versionCode")
        val name = metadata.getString("versionName")
        val hash = metadata.getString("sha256")
        val size = metadata.getLong("size")
        require(metadata.getInt("schemaVersion") == 1 && metadata.getString("applicationId") == BuildConfig.APPLICATION_ID)
        require(metadata.getString("apkName") == UpdatePolicy.APK_NAME && UpdatePolicy.validHash(hash))
        require(code in 1..Int.MAX_VALUE.toLong() && releaseJson.getString("tag_name") == "v$name")
        require(size in 1..UpdatePolicy.MAX_APK_BYTES && size == apk.getLong("size"))
        return AppRelease(code, name, apkUrl, hash, size)
    }

    private suspend fun json(url: String, allowMissing: Boolean = false): JSONObject? {
        val connection = connect(url)
        try {
            if (allowMissing && connection.responseCode == 404) return null
            ensureSuccess(connection)
            val output = java.io.ByteArrayOutputStream()
            connection.inputStream.use { input ->
                val buffer = ByteArray(8192)
                while (true) {
                    coroutineContext.ensureActive()
                    val count = input.read(buffer)
                    if (count < 0) break
                    require(output.size() + count <= 1024 * 1024)
                    output.write(buffer, 0, count)
                }
            }
            return JSONObject(output.toString("UTF-8"))
        } finally { connection.disconnect() }
    }

    suspend fun download(context: Context, release: AppRelease, progress: (Float) -> Unit): File {
        require(UpdatePolicy.isReleaseAsset(release.apkUrl))
        val folder = File(context.cacheDir, "updates").apply { mkdirs() }
        val apk = File(folder, "piccole-gioie.apk")
        val partial = File(folder, "download.part")
        val connection = connect(release.apkUrl)
        try {
            ensureSuccess(connection)
            val digest = MessageDigest.getInstance("SHA-256")
            var total = 0L
            var lastPercent = -1
            connection.inputStream.use { input -> partial.outputStream().use { output ->
                val buffer = ByteArray(32 * 1024)
                while (true) {
                    coroutineContext.ensureActive()
                    val count = input.read(buffer)
                    if (count < 0) break
                    total += count
                    if (total > release.size || total > UpdatePolicy.MAX_APK_BYTES) throw UpdateProblem("Il download non corrisponde alla release. Riprova.")
                    output.write(buffer, 0, count); digest.update(buffer, 0, count)
                    val percent = (total * 100 / release.size).toInt()
                    if (percent != lastPercent) { progress(total.toFloat() / release.size); lastPercent = percent }
                }
            } }
            val hash = digest.digest().joinToString("") { "%02x".format(it) }
            if (total != release.size || !hash.equals(release.sha256, true)) throw UpdateProblem("Il download è incompleto o danneggiato. Riprova.")
            verifyPackage(context, partial, release)
            check(!apk.exists() || apk.delete())
            check(partial.renameTo(apk))
            return apk
        } finally { connection.disconnect(); partial.delete() }
    }

    @Suppress("DEPRECATION")
    fun verifyPackage(context: Context, apk: File, release: AppRelease) {
        val manager = context.packageManager
        val flags = if (Build.VERSION.SDK_INT >= 28) PackageManager.GET_SIGNING_CERTIFICATES else PackageManager.GET_SIGNATURES
        val archive = manager.getPackageArchiveInfo(apk.absolutePath, flags)
            ?: throw UpdateProblem("Il file scaricato non è un aggiornamento valido.")
        val installed = manager.getPackageInfo(context.packageName, flags)
        val archiveCode = if (Build.VERSION.SDK_INT >= 28) archive.longVersionCode else archive.versionCode.toLong()
        if (archive.packageName != context.packageName || archiveCode != release.versionCode ||
            archive.versionName != release.versionName || !UpdatePolicy.isNewer(BuildConfig.VERSION_CODE.toLong(), archiveCode))
            throw UpdateProblem("Questo file non è un aggiornamento di piccole gioie.")
        fun signatures(info: android.content.pm.PackageInfo): Set<String> =
            (if (Build.VERSION.SDK_INT >= 28) info.signingInfo?.apkContentsSigners else info.signatures)
                ?.map { MessageDigest.getInstance("SHA-256").digest(it.toByteArray()).joinToString("") { b -> "%02x".format(b) } }?.toSet().orEmpty()
        val trusted = signatures(installed)
        if (trusted.isEmpty() || signatures(archive) != trusted) throw UpdateProblem("La firma dell’aggiornamento non corrisponde a questa app.")
    }

    private fun connect(url: String): HttpURLConnection {
        var target = url
        repeat(6) {
            require(UpdatePolicy.isGitHubHttps(target))
            val connection = (URL(target).openConnection() as HttpURLConnection).apply {
                connectTimeout = 15_000; readTimeout = 30_000; instanceFollowRedirects = false
                setRequestProperty("User-Agent", "piccole-gioie/${BuildConfig.VERSION_NAME}")
                setRequestProperty("Accept", if (target.startsWith("https://api.github.com/")) "application/vnd.github+json" else "application/octet-stream")
            }
            if (connection.responseCode !in listOf(301, 302, 303, 307, 308)) return connection
            val location = connection.getHeaderField("Location")
            connection.disconnect()
            target = URL(URL(target), requireNotNull(location)).toString()
        }
        throw UpdateProblem("GitHub non riesce a completare il download. Riprova tra poco.")
    }

    private fun ensureSuccess(connection: HttpURLConnection) {
        if (connection.responseCode in listOf(403, 429)) throw UpdateProblem("GitHub chiede una pausa. Riprova tra qualche minuto.")
        if (connection.responseCode != 200) throw UpdateProblem("La release non è disponibile in questo momento. Riprova tra poco.")
    }
}
