package com.tunmed.pillswaterreminder.data.mapper

import com.tunmed.pillswaterreminder.domain.model.TimeDose
import kotlinx.datetime.LocalTime

internal fun List<LocalTime>.encodeTimes(): String = joinToString(",")

internal fun String.decodeTimes(): List<LocalTime> =
    split(",").filter { it.isNotBlank() }.map { LocalTime.parse(it.trim()) }

internal fun Map<LocalTime, TimeDose>.encodeTimeDoses(): String? {
    if (isEmpty()) return null
    return entries.sortedBy { it.key }.joinToString("\n") { (time, dose) ->
        listOf(time.toString(), dose.amount?.toString().orEmpty(), dose.text.replace(LINE_BREAKS_AND_TABS, " "))
            .joinToString("\t")
    }
}

internal fun String?.decodeTimeDoses(): Map<LocalTime, TimeDose> =
    orEmpty().lineSequence().mapNotNull { line ->
        val parts = line.split('\t', limit = 3)
        if (parts.size < 3) return@mapNotNull null
        val time = runCatching { LocalTime.parse(parts[0]) }.getOrNull() ?: return@mapNotNull null
        time to TimeDose(text = parts[2], amount = parts[1].toDoubleOrNull())
    }.toMap()

private val LINE_BREAKS_AND_TABS = Regex("[\t\r\n]")
