package com.tunmed.pillswaterreminder.platform.reminder

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.tunmed.pillswaterreminder.domain.usecase.reminder.RescheduleAllUseCase
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class ReminderReceiver : BroadcastReceiver(), KoinComponent {

    private val rescheduleAll: RescheduleAllUseCase by inject()

    override fun onReceive(context: Context, intent: Intent) {
        runAsync { rescheduleAll() }
    }

    companion object {
        const val ACTION_ALARM = "com.tunmed.pillswaterreminder.action.REMINDER_ALARM"
    }
}
