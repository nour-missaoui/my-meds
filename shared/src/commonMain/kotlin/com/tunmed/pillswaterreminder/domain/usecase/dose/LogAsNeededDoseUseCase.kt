package com.tunmed.pillswaterreminder.domain.usecase.dose

import com.tunmed.pillswaterreminder.core.time.TimeProvider
import com.tunmed.pillswaterreminder.domain.model.DoseLog
import com.tunmed.pillswaterreminder.domain.model.DoseStatus
import com.tunmed.pillswaterreminder.domain.repository.DoseLogRepository
import com.tunmed.pillswaterreminder.domain.repository.MedicationRepository

class LogAsNeededDoseUseCase(
    private val medicationRepository: MedicationRepository,
    private val doseLogRepository: DoseLogRepository,
    private val timeProvider: TimeProvider,
) {
    suspend operator fun invoke(medicationId: Long) {
        val schedule = medicationRepository.getCurrentSchedule(medicationId) ?: return
        val now = timeProvider.now()
        doseLogRepository.insert(
            DoseLog(
                medicationId = medicationId,
                scheduleId = schedule.id,
                scheduledAt = now,
                status = DoseStatus.TAKEN,
                actedAt = now,
            ),
        )
    }
}
