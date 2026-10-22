package it.piccolegioie.app

import java.net.URI

/** Shared by discovery and download: only assets of this public repository are accepted. */
object UpdatePolicy {
    const val REPOSITORY = "carellice/piccole-gioie"
    const val APK_NAME = "piccole-gioie.apk"
    const val METADATA_NAME = "piccole-gioie-update.json"
    const val MAX_APK_BYTES = 100L * 1024 * 1024

    fun isNewer(installed: Long, published: Long) = published > installed && published <= Int.MAX_VALUE

    fun isReleaseAsset(url: String): Boolean = runCatching {
        val uri = URI(url)
        uri.scheme == "https" && uri.host == "github.com" && uri.userInfo == null &&
            uri.port == -1 && uri.path.startsWith("/$REPOSITORY/releases/download/") &&
            uri.query == null && uri.fragment == null
    }.getOrDefault(false)

    fun isGitHubHttps(url: String): Boolean = runCatching {
        val uri = URI(url)
        uri.scheme == "https" && uri.userInfo == null && uri.port == -1 &&
            (uri.host == "github.com" || uri.host == "api.github.com" ||
                uri.host?.endsWith(".githubusercontent.com") == true)
    }.getOrDefault(false)

    fun validHash(hash: String) = hash.matches(Regex("[a-fA-F0-9]{64}"))
}
