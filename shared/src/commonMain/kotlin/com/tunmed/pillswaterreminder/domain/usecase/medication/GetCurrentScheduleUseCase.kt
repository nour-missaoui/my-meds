package com.tunmed.pillswaterreminder.domain.usecase.medication

import com.tunmed.pillswaterreminder.domain.model.MedSchedule
import com.tunmed.pillswaterreminder.domain.repository.MedicationRepository

class GetCurrentScheduleUseCase(private val repository: MedicationRepository) {
    suspend operator fun invoke(medicationId: Long): MedSchedule? = repository.getCurrentSchedule(medicationId)
}
