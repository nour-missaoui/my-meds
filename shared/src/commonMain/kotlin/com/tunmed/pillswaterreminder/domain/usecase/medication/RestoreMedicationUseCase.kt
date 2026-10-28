package com.tunmed.pillswaterreminder.domain.usecase.medication

import com.tunmed.pillswaterreminder.core.time.TimeProvider
import com.tunmed.pillswaterreminder.domain.repository.MedicationRepository
import com.tunmed.pillswaterreminder.domain.usecase.reminder.RescheduleAllUseCase

class RestoreMedicationUseCase(
    private val repository: MedicationRepository,
    private val rescheduleAll: RescheduleAllUseCase,
    private val timeProvider: TimeProvider,
) {
    suspend operator fun invoke(id: Long) {
        rescheduleAll()
        repository.restore(id, timeProvider.today())
        rescheduleAll()
    }
}
