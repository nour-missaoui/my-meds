package com.tunmed.pillswaterreminder.presentation.medications

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tunmed.pillswaterreminder.core.time.TimeProvider
import com.tunmed.pillswaterreminder.domain.model.MedSchedule
import com.tunmed.pillswaterreminder.domain.model.Medication
import com.tunmed.pillswaterreminder.domain.model.MedicationForm
import com.tunmed.pillswaterreminder.domain.model.RecurrenceRule
import com.tunmed.pillswaterreminder.domain.model.TimeDose
import com.tunmed.pillswaterreminder.domain.usecase.medication.DeleteMedicationUseCase
import com.tunmed.pillswaterreminder.domain.usecase.medication.GetCurrentScheduleUseCase
import com.tunmed.pillswaterreminder.domain.usecase.medication.ObserveMedicationUseCase
import com.tunmed.pillswaterreminder.domain.usecase.medication.SaveMedicationUseCase
import com.tunmed.pillswaterreminder.presentation.navigation.EditMedicationRoute
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime

class EditMedicationViewModel(
    private val medicationId: Long,
    observeMedication: ObserveMedicationUseCase,
    private val getCurrentSchedule: GetCurrentScheduleUseCase,
    private val saveMedication: SaveMedicationUseCase,
    private val deleteMedication: DeleteMedicationUseCase,
    private val timeProvider: TimeProvider,
) : ViewModel() {

    val isNew: Boolean = medicationId == EditMedicationRoute.NEW_MEDICATION

    val today: LocalDate = timeProvider.today()

    val medication: StateFlow<Medication?> =
        (if (isNew) flowOf(null) else observeMedication(medicationId))
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val schedule: StateFlow<MedSchedule?> =
        flow { if (!isNew) emit(getCurrentSchedule(medicationId)) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    private var saving = false

    fun save(
        name: String,
        dose: DoseInput,
        times: List<LocalTime>,
        doseByTime: Map<LocalTime, TimeDose>,
        rule: RecurrenceRule,
        headsUpDays: Int,
        onSaved: () -> Unit,
    ) {
        if (saving || (!isNew && medication.value == null)) return
        saving = true
        viewModelScope.launch {
            val today = timeProvider.today()
            val base = medication.value
                ?: Medication(name = name, dose = "", form = MedicationForm.OTHER, startDate = today)
            val withDose = when (dose) {
                is DoseInput.Quick -> base.copy(dose = dose.text.trim(), doseAmount = null, doseUnit = null, strength = null)
                is DoseInput.Detailed -> base.copy(
                    dose = dose.display,
                    form = dose.form,
                    doseAmount = dose.amount,
                    doseUnit = dose.unit,
                    strength = dose.strength?.trim()?.ifEmpty { null },
                )
            }
            val updated = withDose.copy(name = name.trim())
            saveMedication(
                medication = updated,
                schedule = MedSchedule(
                    medicationId = updated.id,
                    rule = rule,
                    times = times,
                    doseByTime = doseByTime.filterKeys { it in times },
                    headsUpDays = headsUpDays,
                    validFrom = today,
                ),
            )
            onSaved()
        }
    }

    fun delete(onDeleted: () -> Unit) {
        if (isNew || saving) return
        saving = true
        viewModelScope.launch {
            deleteMedication(medicationId)
            onDeleted()
        }
    }
}
