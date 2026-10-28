package com.tunmed.pillswaterreminder.domain.usecase.dose

import com.tunmed.pillswaterreminder.domain.model.DoseLog
import com.tunmed.pillswaterreminder.domain.model.DoseStatus
import com.tunmed.pillswaterreminder.domain.reminder.ReminderNotifier
import com.tunmed.pillswaterreminder.domain.repository.DoseLogRepository
import com.tunmed.pillswaterreminder.domain.usecase.reminder.RescheduleAllUseCase

class CorrectDoseLogUseCase(
    private val repository: DoseLogRepository,
    private val notifier: ReminderNotifier,
    private val rescheduleAll: RescheduleAllUseCase,
) {
    suspend operator fun invoke(log: DoseLog, status: DoseStatus) {
        if (log.status == status) return
        val actedAt = when (status) {
            DoseStatus.TAKEN, DoseStatus.SKIPPED -> log.actedAt?.takeIf { log.status == DoseStatus.TAKEN || log.status == DoseStatus.SKIPPED }
                ?: log.scheduledAt
            else -> log.actedAt
        }
        repository.upsert(log.copy(status = status, actedAt = actedAt))
        notifier.cancelDose(log.medicationId, log.scheduledAt)
        rescheduleAll()
    }
}
