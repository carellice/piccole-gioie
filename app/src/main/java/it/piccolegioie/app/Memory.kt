package it.piccolegioie.app

import java.time.LocalDate
import java.time.temporal.ChronoUnit
import java.util.UUID

data class Memory(
    val id: String = UUID.randomUUID().toString(),
    val text: String,
    val date: LocalDate = LocalDate.now(),
    val category: String = "Piccole gioie",
    val color: Int = 0,
    val favorite: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

object MemoryStats {
    fun streak(memories: List<Memory>, today: LocalDate = LocalDate.now()): Int {
        val dates = memories.map { it.date }.toSet()
        var cursor = if (today in dates) today else today.minusDays(1)
        var result = 0
        while (cursor in dates) { result++; cursor = cursor.minusDays(1) }
        return result
    }

    fun yearProgress(today: LocalDate = LocalDate.now()): Float =
        today.dayOfYear.toFloat() / today.lengthOfYear()

    fun daysTogether(memories: List<Memory>, today: LocalDate = LocalDate.now()): Long =
        memories.maxOfOrNull { ChronoUnit.DAYS.between(it.date, today).coerceAtLeast(0) + 1 } ?: 0
}
