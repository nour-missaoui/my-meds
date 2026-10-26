package com.tunmed.pillswaterreminder.data.mapper

import kotlinx.datetime.LocalTime

/** "08:00,10:30" <-> list of times. */
internal fun List<LocalTime>.encodeTimes(): String = joinToString(",")

internal fun String.decodeTimes(): List<LocalTime> =
    split(",").filter { it.isNotBlank() }.map { LocalTime.parse(it.trim()) }
