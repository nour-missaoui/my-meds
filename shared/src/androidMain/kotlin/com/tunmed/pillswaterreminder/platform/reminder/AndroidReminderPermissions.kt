package com.tunmed.pillswaterreminder.platform.reminder

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import androidx.core.app.NotificationManagerCompat
import com.tunmed.pillswaterreminder.domain.reminder.ReminderPermissions
import com.tunmed.pillswaterreminder.domain.reminder.ReminderScheduler
import com.tunmed.pillswaterreminder.platform.notification.NotificationChannels

class AndroidReminderPermissions(
    private val context: Context,
    private val scheduler: ReminderScheduler,
) : ReminderPermissions {

    private val manager = NotificationManagerCompat.from(context)

    override fun notificationsAllowed(): Boolean {
        if (!manager.areNotificationsEnabled()) return false
        val channel = manager.getNotificationChannelCompat(NotificationChannels.MEDICATIONS) ?: return true
        return channel.importance != NotificationManagerCompat.IMPORTANCE_NONE
    }

    override fun exactAlarmsAllowed(): Boolean = scheduler.canScheduleExactAlarms()

    override fun openNotificationSettings() {
        val intent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
        } else {
            appDetailsIntent()
        }
        start(intent)
    }

    override fun openExactAlarmSettings() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            start(Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM, packageUri()))
        }
    }

    override fun batteryOptimizationIgnored(): Boolean {
        val power = context.getSystemService(PowerManager::class.java) ?: return true
        return power.isIgnoringBatteryOptimizations(context.packageName)
    }

    override fun openBatterySettings() {
        start(Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS))
    }

    private fun appDetailsIntent() = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, packageUri())

    private fun packageUri(): Uri = Uri.fromParts("package", context.packageName, null)

    private fun start(intent: Intent) {
        try {
            context.startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        } catch (e: ActivityNotFoundException) {
            context.startActivity(appDetailsIntent().addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        }
    }
}
