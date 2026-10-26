package com.tunmed.pillswaterreminder.presentation.today

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tunmed.pillswaterreminder.core.time.TimeProvider
import com.tunmed.pillswaterreminder.domain.model.MedicationStatus
import com.tunmed.pillswaterreminder.domain.usecase.medication.ObserveMedicationsUseCase
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class TodayViewModel(
    observeMedications: ObserveMedicationsUseCase,
    timeProvider: TimeProvider,
) : ViewModel() {

    val uiState: StateFlow<TodayUiState> = observeMedications()
        .map { medications ->
            TodayUiState(
                date = timeProvider.today(),
                activeMedications = medications.filter { it.status == MedicationStatus.ACTIVE },
            )
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), TodayUiState())
}
