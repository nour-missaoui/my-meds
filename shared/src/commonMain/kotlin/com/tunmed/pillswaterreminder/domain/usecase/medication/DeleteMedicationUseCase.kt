package com.tunmed.pillswaterreminder.domain.usecase.medication

import com.tunmed.pillswaterreminder.domain.model.MedicationStatus
import com.tunmed.pillswaterreminder.domain.reminder.ReminderNotifier

class DeleteMedicationUseCase(
    private val setMedicationStatus: SetMedicationStatusUseCase,
    private val notifier: ReminderNotifier,
) {
    suspend operator fun invoke(medicationId: Long) {
        setMedicationStatus(medicationId, MedicationStatus.ARCHIVED)
        notifier.cancelMedication(medicationId)
    }
}
