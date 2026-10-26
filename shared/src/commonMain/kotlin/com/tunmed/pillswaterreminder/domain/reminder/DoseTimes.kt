package com.tunmed.pillswaterreminder.domain.reminder

import kotlin.math.roundToInt
import kotlinx.datetime.LocalTime

object DoseTimes {
    val FIRST: LocalTime = LocalTime(8, 0)
    val LAST: LocalTime = LocalTime(20, 0)
    const val MAX_PER_DAY = 12

    fun spread(timesPerDay: Int): List<LocalTime> {
        val count = timesPerDay.coerceIn(1, MAX_PER_DAY)
        if (count == 1) return listOf(FIRST)
        val start = FIRST.minutesOfDay()
        val step = (LAST.minutesOfDay() - start).toDouble() / (count - 1)
        return List(count) { i ->
            val minutes = start + roundTo5(i * step)
            LocalTime(minutes / 60, minutes % 60)
        }
    }

    private fun LocalTime.minutesOfDay(): Int = hour * 60 + minute

    private fun roundTo5(minutes: Double): Int = (minutes / 5).roundToInt() * 5
}
