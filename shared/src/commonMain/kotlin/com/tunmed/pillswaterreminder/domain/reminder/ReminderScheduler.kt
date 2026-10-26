package com.tunmed.pillswaterreminder.domain.reminder

import kotlin.time.Instant

data class ScheduledAlarm(val at: Instant)

interface ReminderScheduler {
    fun canScheduleExactAlarms(): Boolean

    fun schedule(alarm: ScheduledAlarm)

    fun cancel()
}
