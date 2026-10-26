package com.tunmed.pillswaterreminder.platform.reminder

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.Uri
import com.tunmed.pillswaterreminder.domain.model.DoseStatus
import com.tunmed.pillswaterreminder.domain.usecase.dose.RecordDoseActionUseCase
import kotlin.time.Instant
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class ReminderActionReceiver : BroadcastReceiver(), KoinComponent {

    private val recordDoseAction: RecordDoseActionUseCase by inject()

    override fun onReceive(context: Context, intent: Intent) {
        val status = when (intent.action) {
            ACTION_TAKEN -> DoseStatus.TAKEN
            ACTION_SKIP -> DoseStatus.SKIPPED
            ACTION_SNOOZE -> DoseStatus.SNOOZED
            else -> return
        }
        val medicationId = intent.getLongExtra(EXTRA_MEDICATION_ID, -1)
        val scheduleId = intent.getLongExtra(EXTRA_SCHEDULE_ID, -1)
        val scheduledAtMillis = intent.getLongExtra(EXTRA_SCHEDULED_AT, -1)
        if (medicationId < 0 || scheduleId < 0 || scheduledAtMillis < 0) return

        runAsync {
            recordDoseAction(
                medicationId = medicationId,
                scheduleId = scheduleId,
                scheduledAt = Instant.fromEpochMilliseconds(scheduledAtMillis),
                status = status,
            )
        }
    }

    companion object {
        const val ACTION_TAKEN = "com.tunmed.pillswaterreminder.action.DOSE_TAKEN"
        const val ACTION_SKIP = "com.tunmed.pillswaterreminder.action.DOSE_SKIP"
        const val ACTION_SNOOZE = "com.tunmed.pillswaterreminder.action.DOSE_SNOOZE"

        private const val EXTRA_MEDICATION_ID = "medication_id"
        private const val EXTRA_SCHEDULE_ID = "schedule_id"
        private const val EXTRA_SCHEDULED_AT = "scheduled_at"

        fun intent(context: Context, action: String, medicationId: Long, scheduleId: Long, scheduledAt: Instant): Intent {
            val millis = scheduledAt.toEpochMilliseconds()
            return Intent(context, ReminderActionReceiver::class.java)
                .setAction(action)
                .setData(Uri.parse("medsreminder://dose/$medicationId/$millis"))
                .putExtra(EXTRA_MEDICATION_ID, medicationId)
                .putExtra(EXTRA_SCHEDULE_ID, scheduleId)
                .putExtra(EXTRA_SCHEDULED_AT, millis)
        }
    }
}
