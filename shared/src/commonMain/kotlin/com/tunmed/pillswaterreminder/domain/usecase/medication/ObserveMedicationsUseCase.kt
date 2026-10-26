package com.tunmed.pillswaterreminder.domain.usecase.medication

import com.tunmed.pillswaterreminder.domain.model.Medication
import com.tunmed.pillswaterreminder.domain.repository.MedicationRepository
import kotlinx.coroutines.flow.Flow

class ObserveMedicationsUseCase(private val repository: MedicationRepository) {
    operator fun invoke(): Flow<List<Medication>> = repository.observeMedications()
}
