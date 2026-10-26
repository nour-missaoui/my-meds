package com.tunmed.pillswaterreminder.presentation.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tunmed.pillswaterreminder.domain.repository.SettingsRepository
import kotlinx.coroutines.launch

class OnboardingViewModel(
    private val settingsRepository: SettingsRepository,
) : ViewModel() {

    fun finish(onDone: () -> Unit) {
        viewModelScope.launch {
            settingsRepository.update { it.copy(onboardingDone = true) }
            onDone()
        }
    }
}
