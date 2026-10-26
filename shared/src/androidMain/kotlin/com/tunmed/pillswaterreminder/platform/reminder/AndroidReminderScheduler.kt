package com.tunmed.pillswaterreminder.platform.reminder

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.tunmed.pillswaterreminder.domain.reminder.ReminderScheduler
import com.tunmed.pillswaterreminder.domain.reminder.ScheduledAlarm

class AndroidReminderScheduler(private val context: Context) : ReminderScheduler {

    private val alarmManager: AlarmManager = context.getSystemService(AlarmManager::class.java)

    override fun canScheduleExactAlarms(): Boolean =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) alarmManager.canScheduleExactAlarms() else true

    override fun schedule(alarm: ScheduledAlarm) {
        val triggerAtMillis = alarm.at.toEpochMilliseconds()
        val pendingIntent = alarmPendingIntent()
        try {
            if (canScheduleExactAlarms()) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
            } else {
                alarmManager.setWindow(AlarmManager.RTC_WAKEUP, triggerAtMillis, INEXACT_WINDOW_MILLIS, pendingIntent)
            }
        } catch (e: SecurityException) {
            alarmManager.setWindow(AlarmManager.RTC_WAKEUP, triggerAtMillis, INEXACT_WINDOW_MILLIS, pendingIntent)
        }
    }

    override fun cancel() {
        alarmManager.cancel(alarmPendingIntent())
    }

    private fun alarmPendingIntent(): PendingIntent = PendingIntent.getBroadcast(
        context,
        REQUEST_CODE,
        Intent(context, ReminderReceiver::class.java).setAction(ReminderReceiver.ACTION_ALARM),
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
    )

    private companion object {
        const val REQUEST_CODE = 1001
        const val INEXACT_WINDOW_MILLIS = 10 * 60 * 1000L
    }
}
