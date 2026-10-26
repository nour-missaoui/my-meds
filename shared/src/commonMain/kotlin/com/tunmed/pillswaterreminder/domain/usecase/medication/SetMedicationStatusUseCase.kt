package com.tunmed.pillswaterreminder.domain.usecase.medication

import com.tunmed.pillswaterreminder.domain.model.MedicationStatus
import com.tunmed.pillswaterreminder.domain.repository.MedicationRepository
import com.tunmed.pillswaterreminder.domain.usecase.reminder.RescheduleAllUseCase

class SetMedicationStatusUseCase(
    private val repository: MedicationRepository,
    private val rescheduleAll: RescheduleAllUseCase,
) {
    suspend operator fun invoke(id: Long, status: MedicationStatus) {
        rescheduleAll()
        repository.setStatus(id, status)
        rescheduleAll()
    }
}
