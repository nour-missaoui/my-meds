package com.tunmed.pillswaterreminder.presentation.medications

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tunmed.pillswaterreminder.core.time.TimeProvider
import com.tunmed.pillswaterreminder.domain.model.MedSchedule
import com.tunmed.pillswaterreminder.domain.model.Medication
import com.tunmed.pillswaterreminder.domain.model.MedicationForm
import com.tunmed.pillswaterreminder.domain.model.RecurrenceRule
import com.tunmed.pillswaterreminder.domain.usecase.medication.ObserveMedicationUseCase
import com.tunmed.pillswaterreminder.domain.usecase.medication.SaveMedicationUseCase
import com.tunmed.pillswaterreminder.presentation.navigation.EditMedicationRoute
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalTime

class EditMedicationViewModel(
    private val medicationId: Long,
    observeMedication: ObserveMedicationUseCase,
    private val saveMedication: SaveMedicationUseCase,
    private val timeProvider: TimeProvider,
) : ViewModel() {

    val isNew: Boolean = medicationId == EditMedicationRoute.NEW_MEDICATION

    val medication: StateFlow<Medication?> =
        (if (isNew) flowOf(null) else observeMedication(medicationId))
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    /**
     * Minimal save used to prove the Room wiring end to end. The full form
     * (form, colour, schedule type, times, heads-up, dates) comes in Phase 3.
     */
    fun save(name: String, dose: String, onSaved: () -> Unit) {
        viewModelScope.launch {
            val today = timeProvider.today()
            val base = medication.value
                ?: Medication(name = name, dose = dose, form = MedicationForm.TABLET, startDate = today)
            val updated = base.copy(name = name.trim(), dose = dose.trim())
            saveMedication(
                medication = updated,
                schedule = MedSchedule(
                    medicationId = updated.id,
                    rule = RecurrenceRule.Daily,
                    times = listOf(LocalTime(8, 0)),
                    validFrom = today,
                ),
            )
            onSaved()
        }
    }
}
