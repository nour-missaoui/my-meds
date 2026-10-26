package com.tunmed.pillswaterreminder.platform.notification

import android.content.Context
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationManagerCompat

/** Channel ids are part of the user's system settings: never rename them after release. */
object NotificationChannels {
    const val MEDICATIONS = "medications"
    const val UPCOMING = "upcoming"

    /** Created by early debug builds before the meds-only scope; removed on start. */
    private const val LEGACY_WATER = "water"

    fun createAll(context: Context) {
        val manager = NotificationManagerCompat.from(context)
        val channels = listOf(
            NotificationChannelCompat.Builder(MEDICATIONS, NotificationManagerCompat.IMPORTANCE_HIGH)
                .setName("Medications")
                .setDescription("Dose reminders with Taken, Snooze and Skip")
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
