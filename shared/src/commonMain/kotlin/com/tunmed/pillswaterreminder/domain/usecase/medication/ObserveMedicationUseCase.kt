package com.tunmed.pillswaterreminder.domain.usecase.medication

import com.tunmed.pillswaterreminder.domain.model.Medication
import com.tunmed.pillswaterreminder.domain.repository.MedicationRepository
import kotlinx.coroutines.flow.Flow

class ObserveMedicationUseCase(private val repository: MedicationRepository) {
    operator fun invoke(id: Long): Flow<Medication?> = repository.observeMedication(id)
}
