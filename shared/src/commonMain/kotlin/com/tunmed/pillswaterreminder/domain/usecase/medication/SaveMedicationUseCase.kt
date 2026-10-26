package com.tunmed.pillswaterreminder.domain.usecase.medication

import com.tunmed.pillswaterreminder.domain.model.MedSchedule
import com.tunmed.pillswaterreminder.domain.model.Medication
import com.tunmed.pillswaterreminder.domain.repository.MedicationRepository
import com.tunmed.pillswaterreminder.domain.usecase.reminder.RescheduleAllUseCase

class SaveMedicationUseCase(
    private val repository: MedicationRepository,
    private val rescheduleAll: RescheduleAllUseCase,
) {
    suspend operator fun invoke(medication: Medication, schedule: MedSchedule): Long {
        rescheduleAll()
        val id = repository.save(medication, schedule)
        rescheduleAll()
        return id
    }
}
