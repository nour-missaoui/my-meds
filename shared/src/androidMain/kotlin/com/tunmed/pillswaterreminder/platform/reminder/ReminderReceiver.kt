package com.tunmed.pillswaterreminder.platform.reminder

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.tunmed.pillswaterreminder.domain.usecase.reminder.RescheduleAllUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/** Fired by AlarmManager at the next reminder time. */
class ReminderReceiver : BroadcastReceiver(), KoinComponent {

    private val rescheduleAll: RescheduleAllUseCase by inject()

    override fun onReceive(context: Context, intent: Intent) {
        val pendingResult = goAsync()
        CoroutineScope(SupervisorJob() + Dispatchers.Default).launch {
            try {
                // TODO(Phase 2): find reminders due now, write PENDING DoseLogs and post
                //  notifications (channels in NotificationChannels) with action buttons.
                rescheduleAll()
            } finally {
                pendingResult.finish()
            }
        }
    }

    companion object {
        const val ACTION_ALARM = "com.tunmed.pillswaterreminder.action.REMINDER_ALARM"
    }
}
