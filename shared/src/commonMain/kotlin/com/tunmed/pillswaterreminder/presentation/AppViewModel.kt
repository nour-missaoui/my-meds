package com.tunmed.pillswaterreminder.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tunmed.pillswaterreminder.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class AppViewModel(settingsRepository: SettingsRepository) : ViewModel() {
    val onboardingDone: StateFlow<Boolean?> = settingsRepository.observe()
        .map { it.onboardingDone }
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)
}
