package com.tunmed.pillswaterreminder.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tunmed.pillswaterreminder.domain.model.AppSettings
import com.tunmed.pillswaterreminder.domain.reminder.ReminderPermissions
import com.tunmed.pillswaterreminder.domain.repository.SettingsRepository
import com.tunmed.pillswaterreminder.domain.usecase.settings.UpdateSettingsUseCase
import com.tunmed.pillswaterreminder.presentation.permissions.PermissionStatus
import com.tunmed.pillswaterreminder.presentation.permissions.status
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(
    settingsRepository: SettingsRepository,
    private val updateSettings: UpdateSettingsUseCase,
    private val permissions: ReminderPermissions,
) : ViewModel() {

    val settings: StateFlow<AppSettings> = settingsRepository.observe()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AppSettings())

    private val _permissionStatus = MutableStateFlow(permissions.status())
    val permissionStatus: StateFlow<PermissionStatus> = _permissionStatus.asStateFlow()

    fun refresh() {
        _permissionStatus.value = permissions.status()
    }

    fun setSnoozeMinutes(minutes: Int) = update { it.copy(snoozeMinutes = minutes) }

    fun setMissedAfterMinutes(minutes: Int) = update { it.copy(missedAfterMinutes = minutes) }

    fun setSound(enabled: Boolean) = update { it.copy(soundEnabled = enabled) }

    fun setVibration(enabled: Boolean) = update { it.copy(vibrationEnabled = enabled) }

    fun openNotificationSettings() = permissions.openNotificationSettings()

    fun openExactAlarmSettings() = permissions.openExactAlarmSettings()

    fun openBatterySettings() = permissions.openBatterySettings()

    private fun update(transform: (AppSettings) -> AppSettings) {
        viewModelScope.launch { updateSettings(transform) }
    }

    companion object {
        val SNOOZE_CHOICES = listOf(5, 10, 15, 30)
        val MISSED_AFTER_CHOICES = listOf(30, 60, 120, 240)
    }
}
