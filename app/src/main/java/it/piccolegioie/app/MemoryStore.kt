package it.piccolegioie.app

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.time.LocalDate

class MemoryStore(context: Context) {
    private val prefs = context.getSharedPreferences("piccole_gioie", Context.MODE_PRIVATE)
    private val file = java.io.File(context.filesDir, "momenti.json")
    private val atomic = android.util.AtomicFile(file)

    fun read(): List<Memory> = if (!file.exists() && !java.io.File(file.path + ".bak").exists()) {
        emptyList()
    } else atomic.openRead().bufferedReader().use { decode(it.readText()) }

    fun save(memories: List<Memory>) {
        val stream = atomic.startWrite()
        try {
            stream.write(encode(memories).toByteArray(Charsets.UTF_8))
            atomic.finishWrite(stream)
        } catch (error: Exception) {
            atomic.failWrite(stream)
            throw error
        }
    }

    var onboardingCompleted: Boolean
        get() = prefs.getBoolean("onboarding_completed", false)
        set(value) { prefs.edit().putBoolean("onboarding_completed", value).apply() }

    var reminderEnabled: Boolean
        get() = prefs.getBoolean("reminder", false)
        set(value) { prefs.edit().putBoolean("reminder", value).apply() }
    var reminderHour: Int
        get() = prefs.getInt("hour", 21)
        set(value) { prefs.edit().putInt("hour", value).apply() }
    var reminderMinute: Int
        get() = prefs.getInt("minute", 0)
        set(value) { prefs.edit().putInt("minute", value).apply() }

    companion object {
        fun encode(memories: List<Memory>): String = JSONObject().apply {
            put("format", "piccole-gioie")
            put("version", 1)
            put("memories", JSONArray().apply {
                memories.forEach { memory -> put(JSONObject().apply {
                    put("id", memory.id); put("text", memory.text)
                    put("date", memory.date.toString()); put("category", memory.category)
                    put("color", memory.color); put("favorite", memory.favorite)
                    put("createdAt", memory.createdAt)
                }) }
            })
        }.toString(2)

        fun decode(json: String): List<Memory> {
            val root = JSONObject(json)
            require(root.getString("format") == "piccole-gioie") { "Formato non riconosciuto" }
            require(root.getInt("version") == 1) { "Versione non supportata" }
            val array = root.getJSONArray("memories")
            require(array.length() <= 50000) { "Troppi ricordi" }
            return (0 until array.length()).map { index ->
                val item = array.getJSONObject(index)
                val text = item.getString("text").trim()
                require(text.isNotEmpty() && text.length <= 2000)
                Memory(
                    id = item.getString("id"), text = text,
                    date = LocalDate.parse(item.getString("date")),
                    category = item.getString("category"),
                    color = item.getInt("color").coerceIn(0, 4),
                    favorite = item.optBoolean("favorite"),
                    createdAt = item.optLong("createdAt", System.currentTimeMillis())
                )
            }.distinctBy { it.id }.sortedByDescending { it.createdAt }
        }
    }
}
