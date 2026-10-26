package com.tunmed.pillswaterreminder.core.time

import kotlin.time.Clock
import kotlin.time.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

interface TimeProvider {
    fun now(): Instant
    fun timeZone(): TimeZone
    fun today(): LocalDate = now().toLocalDateTime(timeZone()).date
}

class SystemTimeProvider : TimeProvider {
    override fun now(): Instant = Clock.System.now()
    override fun timeZone(): TimeZone = TimeZone.currentSystemDefault()
}
