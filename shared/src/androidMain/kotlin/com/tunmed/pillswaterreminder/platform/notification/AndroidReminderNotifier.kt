package com.tunmed.pillswaterreminder.platform.notification

import android.Manifest
import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.text.format.DateUtils
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.tunmed.pillswaterreminder.core.time.toHhMm
import com.tunmed.pillswaterreminder.domain.reminder.DueDose
import com.tunmed.pillswaterreminder.domain.reminder.ReminderNotifier
import com.tunmed.pillswaterreminder.domain.reminder.UpcomingDose
import com.tunmed.pillswaterreminder.platform.reminder.ReminderActionReceiver
import com.tunmed.pillswaterreminder.shared.R
import kotlin.time.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant

class AndroidReminderNotifier(private val context: Context) : ReminderNotifier {

    private val manager = NotificationManagerCompat.from(context)
    private val systemManager: NotificationManager = context.getSystemService(NotificationManager::class.java)

    override fun showDose(dose: DueDose) {
        val medication = dose.medication
        val time = dose.time.toHhMm()

        val publicVersion = doseBuilder(dose)
            .setContentTitle(context.getString(R.string.notif_public_title))
            .setContentText(time)
            .build()

        val notification = doseBuilder(dose)
            .setContentTitle(medication.name)
            .setContentText(context.getString(R.string.notif_dose_text, dose.doseText, time))
            .setVisibility(NotificationCompat.VISIBILITY_PRIVATE)
            .setPublicVersion(publicVersion)
            .build()

        post(TAG_DOSE, medication.id.toInt(), notification)
        post(null, SUMMARY_ID, summary())
    }

    override fun showUpcoming(upcoming: UpcomingDose) {
        val dueAtMillis = upcoming.dueAt.toInstant(TimeZone.currentSystemDefault()).toEpochMilliseconds()
        val dateText = DateUtils.formatDateTime(
            context,
            dueAtMillis,
            DateUtils.FORMAT_SHOW_WEEKDAY or DateUtils.FORMAT_SHOW_DATE or DateUtils.FORMAT_SHOW_TIME or DateUtils.FORMAT_ABBREV_ALL,
        )
        val notification = NotificationCompat.Builder(context, NotificationChannels.UPCOMING)
            .setSmallIcon(R.drawable.ic_stat_pill)
            .setContentTitle(context.getString(R.string.notif_upcoming_title, upcoming.medication.name))
            .setContentText(
                context.resources.getQuantityString(R.plurals.notif_upcoming_text, upcoming.daysAhead, upcoming.daysAhead, dateText),
            )
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setVisibility(NotificationCompat.VISIBILITY_PRIVATE)
            .setContentIntent(openAppIntent())
            .setAutoCancel(true)
            .build()
        post(TAG_UPCOMING, upcoming.medication.id.toInt(), notification)
    }

    override fun cancelDose(medicationId: Long, scheduledAt: Instant) {
        val id = medicationId.toInt()
        val active = activeNotifications()
        val shown = active.firstOrNull { it.tag == TAG_DOSE && it.id == id }
        if (shown != null && shown.notification.`when` != scheduledAt.toEpochMilliseconds()) return
        manager.cancel(TAG_DOSE, id)
        if (active.none { it.tag == TAG_DOSE && it.id != id }) manager.cancel(SUMMARY_ID)
    }

    override fun cancelMedication(medicationId: Long) {
        val id = medicationId.toInt()
        val active = activeNotifications()
        manager.cancel(TAG_DOSE, id)
        manager.cancel(TAG_UPCOMING, id)
        if (active.none { it.tag == TAG_DOSE && it.id != id }) manager.cancel(SUMMARY_ID)
    }

    private fun doseBuilder(dose: DueDose): NotificationCompat.Builder =
        NotificationCompat.Builder(context, NotificationChannels.MEDICATIONS)
            .setSmallIcon(R.drawable.ic_stat_pill)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setWhen(dose.scheduledAt.toEpochMilliseconds())
            .setShowWhen(true)
            .setSilent(dose.silent)
            .setGroup(GROUP_DOSES)
            .setContentIntent(openAppIntent())
            .setAutoCancel(false)
            .addAction(0, context.getString(R.string.notif_action_taken), actionIntent(ReminderActionReceiver.ACTION_TAKEN, dose))
            .addAction(0, context.getString(R.string.notif_action_snooze), actionIntent(ReminderActionReceiver.ACTION_SNOOZE, dose))
            .addAction(0, context.getString(R.string.notif_action_skip), actionIntent(ReminderActionReceiver.ACTION_SKIP, dose))

    private fun summary(): Notification =
        NotificationCompat.Builder(context, NotificationChannels.MEDICATIONS)
            .setSmallIcon(R.drawable.ic_stat_pill)
            .setContentTitle(context.getString(R.string.notif_summary_title))
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setGroup(GROUP_DOSES)
            .setGroupSummary(true)
            .setGroupAlertBehavior(NotificationCompat.GROUP_ALERT_CHILDREN)
            .setContentIntent(openAppIntent())
            .build()

    private fun actionIntent(action: String, dose: DueDose): PendingIntent =
        PendingIntent.getBroadcast(
            context,
            0,
            ReminderActionReceiver.intent(context, action, dose.medication.id, dose.scheduleId, dose.scheduledAt),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

    private fun openAppIntent(): PendingIntent? {
        val launch = context.packageManager.getLaunchIntentForPackage(context.packageName) ?: return null
        launch.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED)
        return PendingIntent.getActivity(context, 0, launch, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
    }

    @SuppressLint("MissingPermission")
    private fun post(tag: String?, id: Int, notification: Notification) {
        if (!canPost()) return
        try {
            manager.notify(tag, id, notification)
        } catch (e: SecurityException) {
        }
    }

    private fun canPost(): Boolean {
        if (!manager.areNotificationsEnabled()) return false
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
    }

    private fun activeNotifications() =
        try {
            systemManager.activeNotifications.toList()
        } catch (e: RuntimeException) {
            emptyList()
        }

    private companion object {
        const val TAG_DOSE = "dose"
        const val TAG_UPCOMING = "upcoming"
        const val SUMMARY_ID = 1
        const val GROUP_DOSES = "com.tunmed.pillswaterreminder.DOSES"
    }
}
