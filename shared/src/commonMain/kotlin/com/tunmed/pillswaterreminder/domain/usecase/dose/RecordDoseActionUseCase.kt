package com.tunmed.pillswaterreminder.domain.usecase.dose

import com.tunmed.pillswaterreminder.core.time.TimeProvider
import com.tunmed.pillswaterreminder.domain.model.DoseLog
import com.tunmed.pillswaterreminder.domain.model.DoseStatus
import com.tunmed.pillswaterreminder.domain.reminder.ReminderNotifier
import com.tunmed.pillswaterreminder.domain.repository.DoseLogRepository
import com.tunmed.pillswaterreminder.domain.usecase.reminder.RescheduleAllUseCase
import kotlin.time.Instant

class RecordDoseActionUseCase(
    private val repository: DoseLogRepository,
    private val notifier: ReminderNotifier,
    private val timeProvider: TimeProvider,
    private val rescheduleAll: RescheduleAllUseCase,
) {
    suspend operator fun invoke(medicationId: Long, scheduleId: Long, scheduledAt: Instant, status: DoseStatus) {
        val base = repository.getForSlot(medicationId, scheduledAt)
            ?: DoseLog(medicationId = medicationId, scheduleId = scheduleId, scheduledAt = scheduledAt, status = status)
        repository.upsert(base.copy(status = status, actedAt = timeProvider.now()))
        notifier.cancelDose(medicationId, scheduledAt)
        rescheduleAll()
    }
}
