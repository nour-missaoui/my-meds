package com.tunmed.pillswaterreminder.domain.reminder

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.datetime.LocalTime

class DoseTimesTest {

    private fun times(vararg hhmm: String) = hhmm.map { LocalTime.parse(it) }

    @Test
    fun one_to_four_times_a_day_use_round_hours() {
        assertEquals(times("08:00"), DoseTimes.spread(1))
        assertEquals(times("08:00", "20:00"), DoseTimes.spread(2))
        assertEquals(times("08:00", "14:00", "20:00"), DoseTimes.spread(3))
        assertEquals(times("08:00", "12:00", "16:00", "20:00"), DoseTimes.spread(4))
    }

    @Test
    fun six_times_a_day_is_rounded_to_5_minutes() {
        assertEquals(times("08:00", "10:25", "12:50", "15:10", "17:35", "20:00"), DoseTimes.spread(6))
    }

    @Test
    fun every_count_gives_distinct_sorted_times_inside_the_day_window() {
        for (n in 1..DoseTimes.MAX_PER_DAY) {
            val spread = DoseTimes.spread(n)
            assertEquals(n, spread.size)
            assertEquals(spread.sorted().distinct(), spread, "n = $n")
            assertEquals(DoseTimes.FIRST, spread.first())
            if (n > 1) assertEquals(DoseTimes.LAST, spread.last())
        }
    }

    @Test
    fun out_of_range_counts_are_clamped() {
        assertEquals(1, DoseTimes.spread(0).size)
        assertEquals(DoseTimes.MAX_PER_DAY, DoseTimes.spread(99).size)
    }
}
