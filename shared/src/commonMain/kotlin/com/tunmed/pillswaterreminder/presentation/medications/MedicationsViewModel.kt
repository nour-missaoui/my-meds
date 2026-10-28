package com.tunmed.pillswaterreminder.presentation.medications

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tunmed.pillswaterreminder.core.time.TimeProvider
import com.tunmed.pillswaterreminder.domain.model.MedicationOverview
import com.tunmed.pillswaterreminder.domain.model.MedicationStatus
import com.tunmed.pillswaterreminder.domain.usecase.medication.DeleteMedicationUseCase
import com.tunmed.pillswaterreminder.domain.usecase.medication.ObserveMedicationOverviewUseCase
import com.tunmed.pillswaterreminder.domain.usecase.medication.RestoreMedicationUseCase
import com.tunmed.pillswaterreminder.domain.usecase.medication.SetMedicationStatusUseCase
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate

data class MedicationsUiState(
    val loaded: Boolean = false,
    val today: LocalDate? = null,
    val active: List<MedicationOverview> = emptyList(),
    val paused: List<MedicationOverview> = emptyList(),
    val archived: List<MedicationOverview> = emptyList(),
) {
    val listedCount: Int get() = active.size + paused.size
}

class MedicationsViewModel(
    observeOverview: ObserveMedicationOverviewUseCase,
    private val setStatus: SetMedicationStatusUseCase,
    private val deleteMedication: DeleteMedicationUseCase,
    private val restoreMedication: RestoreMedicationUseCase,
    private val timeProvider: TimeProvider,
) : ViewModel() {

    val state: StateFlow<MedicationsUiState> = observeOverview()
        .map { list ->
            MedicationsUiState(
                loaded = true,
                today = timeProvider.today(),
                active = list.filter { it.medication.status == MedicationStatus.ACTIVE },
                paused = list.filter { it.medication.status == MedicationStatus.PAUSED },
                archived = list.filter { it.medication.status == MedicationStatus.ARCHIVED },
            )
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), MedicationsUiState())

    fun pause(id: Long) = launch { setStatus(id, MedicationStatus.PAUSED) }

    fun resume(id: Long) = launch { setStatus(id, MedicationStatus.ACTIVE) }

    fun delete(id: Long) = launch { deleteMedication(id) }

    fun restore(id: Long) = launch { restoreMedication(id) }

    private fun launch(block: suspend () -> Unit) {
        viewModelScope.launch { block() }
    }
}
