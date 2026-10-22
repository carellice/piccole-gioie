package it.piccolegioie.app

import org.junit.Assert.*
import org.junit.Test
import java.time.LocalDate

class MemoryStatsTest {
    private val today = LocalDate.of(2026, 10, 22)
    private fun note(daysAgo: Long) = Memory(text = "Un bel momento", date = today.minusDays(daysAgo))

    @Test fun multipleNotesOnSameDayCountOnce() {
        assertEquals(2, MemoryStats.streak(listOf(note(0), note(0), note(1), note(4)), today))
    }
    @Test fun yesterdayKeepsStreakAlive() {
        assertEquals(3, MemoryStats.streak(listOf(note(1), note(2), note(3)), today))
    }
    @Test fun gapResetsStreak() {
        assertEquals(0, MemoryStats.streak(listOf(note(2), note(3)), today))
    }
    @Test fun leapYearProgressEndsAtOne() {
        assertEquals(1f, MemoryStats.yearProgress(LocalDate.of(2028, 12, 31)))
    }
    @Test fun emptyJarHasNoStreak() { assertEquals(0, MemoryStats.streak(emptyList(), today)) }
}
