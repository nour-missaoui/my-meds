package com.tunmed.pillswaterreminder.presentation.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tunmed.pillswaterreminder.presentation.components.PermissionRow
import com.tunmed.pillswaterreminder.presentation.components.ScreenHeader
import com.tunmed.pillswaterreminder.presentation.components.SelectField
import com.tunmed.pillswaterreminder.presentation.permissions.rememberNotificationPermissionRequest
import com.tunmed.pillswaterreminder.presentation.theme.AppTheme
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import pillswaterreminder.shared.generated.resources.Res
import pillswaterreminder.shared.generated.resources.action_allow
import pillswaterreminder.shared.generated.resources.action_open_settings
import pillswaterreminder.shared.generated.resources.action_turn_on
import pillswaterreminder.shared.generated.resources.hours_count
import pillswaterreminder.shared.generated.resources.minutes_count
import pillswaterreminder.shared.generated.resources.perm_allowed
import pillswaterreminder.shared.generated.resources.perm_battery
import pillswaterreminder.shared.generated.resources.perm_battery_ok
import pillswaterreminder.shared.generated.resources.perm_battery_restricted
import pillswaterreminder.shared.generated.resources.perm_battery_tip
import pillswaterreminder.shared.generated.resources.perm_exact_alarms
import pillswaterreminder.shared.generated.resources.perm_notifications
import pillswaterreminder.shared.generated.resources.perm_off
import pillswaterreminder.shared.generated.resources.privacy_body
import pillswaterreminder.shared.generated.resources.settings_missed_after
import pillswaterreminder.shared.generated.resources.settings_permissions
import pillswaterreminder.shared.generated.resources.settings_privacy
import pillswaterreminder.shared.generated.resources.settings_reminders
import pillswaterreminder.shared.generated.resources.settings_snooze
import pillswaterreminder.shared.generated.resources.settings_sound
import pillswaterreminder.shared.generated.resources.settings_sound_body
import pillswaterreminder.shared.generated.resources.settings_vibration
import pillswaterreminder.shared.generated.resources.settings_vibration_body
import pillswaterreminder.shared.generated.resources.title_settings

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = koinViewModel(),
) {
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val status by viewModel.permissionStatus.collectAsStateWithLifecycle()

    LifecycleResumeEffect(Unit) {
        viewModel.refresh()
        onPauseOrDispose { }
    }

    val requestNotifications = rememberNotificationPermissionRequest { granted ->
        if (!granted) viewModel.openNotificationSettings()
        viewModel.refresh()
    }

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = AppTheme.spacing.l),
        verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.m),
    ) {
        ScreenHeader(title = stringResource(Res.string.title_settings))

        SettingsCard(stringResource(Res.string.settings_reminders)) {
            SelectField(
                label = stringResource(Res.string.settings_snooze),
                value = durationLabel(settings.snoozeMinutes),
                options = SettingsViewModel.SNOOZE_CHOICES,
                optionLabel = { durationLabel(it) },
                onSelect = viewModel::setSnoozeMinutes,
                modifier = Modifier.fillMaxWidth(),
            )
            SelectField(
                label = stringResource(Res.string.settings_missed_after),
                value = durationLabel(settings.missedAfterMinutes),
                options = SettingsViewModel.MISSED_AFTER_CHOICES,
                optionLabel = { durationLabel(it) },
                onSelect = viewModel::setMissedAfterMinutes,
                modifier = Modifier.fillMaxWidth(),
            )
            SwitchRow(
                title = stringResource(Res.string.settings_sound),
                body = stringResource(Res.string.settings_sound_body),
                checked = settings.soundEnabled,
                onCheckedChange = viewModel::setSound,
            )
            SwitchRow(
                title = stringResource(Res.string.settings_vibration),
                body = stringResource(Res.string.settings_vibration_body),
                checked = settings.vibrationEnabled,
                onCheckedChange = viewModel::setVibration,
            )
        }

        SettingsCard(stringResource(Res.string.settings_permissions)) {
            PermissionRow(
                title = stringResource(Res.string.perm_notifications),
                statusText = stringResource(if (status.notifications) Res.string.perm_allowed else Res.string.perm_off),
                ok = status.notifications,
                actionLabel = stringResource(Res.string.action_turn_on),
                onAction = requestNotifications,
            )
            PermissionRow(
                title = stringResource(Res.string.perm_exact_alarms),
                statusText = stringResource(if (status.exactAlarms) Res.string.perm_allowed else Res.string.perm_off),
                ok = status.exactAlarms,
                actionLabel = stringResource(Res.string.action_allow),
                onAction = viewModel::openExactAlarmSettings,
            )
            PermissionRow(
                title = stringResource(Res.string.perm_battery),
                statusText = stringResource(if (status.batteryUnrestricted) Res.string.perm_battery_ok else Res.string.perm_battery_restricted),
                ok = status.batteryUnrestricted,
                actionLabel = stringResource(Res.string.action_open_settings),
                onAction = viewModel::openBatterySettings,
                detail = stringResource(Res.string.perm_battery_tip),
            )
        }

        SettingsCard(stringResource(Res.string.settings_privacy)) {
            Text(
                stringResource(Res.string.privacy_body),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Spacer(Modifier.height(AppTheme.spacing.xl))
    }
}

@Composable
private fun SettingsCard(title: String, content: @Composable () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Column(Modifier.padding(AppTheme.spacing.l), verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.m)) {
            Text(title, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)
            content()
        }
    }
}

@Composable
private fun SwitchRow(title: String, body: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge)
            Text(body, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
private fun durationLabel(minutes: Int): String =
    if (minutes >= 60 && minutes % 60 == 0) {
        pluralStringResource(Res.plurals.hours_count, minutes / 60, minutes / 60)
    } else {
        pluralStringResource(Res.plurals.minutes_count, minutes, minutes)
    }
