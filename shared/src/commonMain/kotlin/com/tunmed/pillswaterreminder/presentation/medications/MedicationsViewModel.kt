package com.tunmed.pillswaterreminder.presentation.medications

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tunmed.pillswaterreminder.domain.model.Medication
import com.tunmed.pillswaterreminder.domain.usecase.medication.ObserveMedicationsUseCase
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class MedicationsViewModel(
    observeMedications: ObserveMedicationsUseCase,
) : ViewModel() {
    val medications: StateFlow<List<Medication>> = observeMedications()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
}
