package com.tunmed.pillswaterreminder.presentation.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tunmed.pillswaterreminder.domain.reminder.ReminderPermissions
import com.tunmed.pillswaterreminder.domain.repository.SettingsRepository
import com.tunmed.pillswaterreminder.presentation.permissions.PermissionStatus
import com.tunmed.pillswaterreminder.presentation.permissions.status
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class OnboardingViewModel(
    private val settingsRepository: SettingsRepository,
    private val permissions: ReminderPermissions,
) : ViewModel() {

    private val _permissionStatus = MutableStateFlow(permissions.status())
    val permissionStatus: StateFlow<PermissionStatus> = _permissionStatus.asStateFlow()

    fun refresh() {
        _permissionStatus.value = permissions.status()
    }

    fun openNotificationSettings() = permissions.openNotificationSettings()

    fun openExactAlarmSettings() = permissions.openExactAlarmSettings()

    fun openBatterySettings() = permissions.openBatterySettings()

    fun finish(onDone: () -> Unit) {
        viewModelScope.launch {
            settingsRepository.update { it.copy(onboardingDone = true) }
            onDone()
        }
    }
}
