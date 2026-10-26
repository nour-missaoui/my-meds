package com.tunmed.pillswaterreminder.core.time

import kotlinx.datetime.LocalTime

fun LocalTime.toHhMm(): String = "${hour.pad()}:${minute.pad()}"

private fun Int.pad(): String = toString().padStart(2, '0')

fun parseTime(text: String): LocalTime? {
    val match = TIME_REGEX.matchEntire(text.trim()) ?: return null
    val hour = match.groupValues[1].toInt()
    val minute = match.groupValues[2].toInt()
    if (hour > 23 || minute > 59) return null
    return LocalTime(hour, minute)
}

private val TIME_REGEX = Regex("""(\d{1,2})[:.h](\d{2})""")
