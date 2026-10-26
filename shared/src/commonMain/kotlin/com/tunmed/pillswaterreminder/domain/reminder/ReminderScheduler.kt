package com.tunmed.pillswaterreminder.domain.reminder

import kotlin.time.Instant

/** The next moment the app must wake up and show reminders. */
data class ScheduledAlarm(val at: Instant)

/**
 * Platform port for arming the single "next" alarm.
 * Android: AlarmManager (shared/androidMain). iOS: UNUserNotificationCenter (later).
 */
interface ReminderScheduler {
    /** False when the user has not granted "Alarms & reminders"; the app then falls back to inexact alarms. */
    fun canScheduleExactAlarms(): Boolean

    /** Replaces any previously armed alarm. */
    fun schedule(alarm: ScheduledAlarm)

    fun cancel()
}
