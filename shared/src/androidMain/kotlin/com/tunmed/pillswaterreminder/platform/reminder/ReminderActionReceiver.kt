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

/** Notification buttons: Taken / Snooze / Skip (spec R-1, R-2). */
class ReminderActionReceiver : BroadcastReceiver(), KoinComponent {

    private val rescheduleAll: RescheduleAllUseCase by inject()

    override fun onReceive(context: Context, intent: Intent) {
        val pendingResult = goAsync()
        CoroutineScope(SupervisorJob() + Dispatchers.Default).launch {
            try {
                when (intent.action) {
                    ACTION_TAKEN, ACTION_SKIP, ACTION_SNOOZE -> {
                        // TODO(Phase 2): RecordDoseActionUseCase for the dose in EXTRA_DOSE_LOG_ID,
                        //  cancel the notification; Snooze = one-off alarm in settings.snoozeMinutes.
                    }
                }
                rescheduleAll()
            } finally {
                pendingResult.finish()
            }
        }
    }

    companion object {
        const val ACTION_TAKEN = "com.tunmed.pillswaterreminder.action.DOSE_TAKEN"
        const val ACTION_SKIP = "com.tunmed.pillswaterreminder.action.DOSE_SKIP"
        const val ACTION_SNOOZE = "com.tunmed.pillswaterreminder.action.DOSE_SNOOZE"
        const val EXTRA_DOSE_LOG_ID = "dose_log_id"
    }
}
