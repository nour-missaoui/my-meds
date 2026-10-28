package com.tunmed.pillswaterreminder.platform.notification

import android.content.Context
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationManagerCompat

object NotificationChannels {
    const val MEDICATIONS = "medications"
    const val MEDICATIONS_NO_VIBRATION = "medications_no_vibration"
    const val UPCOMING = "upcoming"

    private const val LEGACY_WATER = "water"

    fun createAll(context: Context) {
        val manager = NotificationManagerCompat.from(context)
        val channels = listOf(
            NotificationChannelCompat.Builder(MEDICATIONS, NotificationManagerCompat.IMPORTANCE_HIGH)
                .setName("Medications")
                .setDescription("Dose reminders with Taken, Snooze and Skip")
                .build(),
            NotificationChannelCompat.Builder(MEDICATIONS_NO_VIBRATION, NotificationManagerCompat.IMPORTANCE_HIGH)
                .setName("Medications (no vibration)")
                .setDescription("Dose reminders when vibration is off in the app's settings")
                .setVibrationEnabled(false)
                .build(),
            NotificationChannelCompat.Builder(UPCOMING, NotificationManagerCompat.IMPORTANCE_LOW)
                .setName("Upcoming")
                .setDescription("Heads-up for monthly and yearly medications")
                .build(),
        )
        manager.createNotificationChannelsCompat(channels)
        manager.deleteNotificationChannel(LEGACY_WATER)
    }
}
