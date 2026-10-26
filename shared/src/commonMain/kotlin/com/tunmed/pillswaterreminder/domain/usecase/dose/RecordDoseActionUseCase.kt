package com.tunmed.pillswaterreminder.domain.usecase.dose

import com.tunmed.pillswaterreminder.core.time.TimeProvider
import com.tunmed.pillswaterreminder.domain.model.DoseLog
import com.tunmed.pillswaterreminder.domain.model.DoseStatus
import com.tunmed.pillswaterreminder.domain.repository.DoseLogRepository
import com.tunmed.pillswaterreminder.domain.usecase.reminder.RescheduleAllUseCase

/** Taken / Skipped / Snoozed, from the Today screen or a notification action. */
class RecordDoseActionUseCase(
    private val repository: DoseLogRepository,
    private val timeProvider: TimeProvider,
    private val rescheduleAll: RescheduleAllUseCase,
) {
    suspend operator fun invoke(log: DoseLog, status: DoseStatus) {
        repository.upsert(log.copy(status = status, actedAt = timeProvider.now()))
        rescheduleAll()
    }
}
