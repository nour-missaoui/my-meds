package com.tunmed.pillswaterreminder.presentation.onboarding

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tunmed.pillswaterreminder.presentation.components.PermissionRow
import com.tunmed.pillswaterreminder.presentation.permissions.PermissionStatus
import com.tunmed.pillswaterreminder.presentation.permissions.rememberNotificationPermissionRequest
import com.tunmed.pillswaterreminder.presentation.theme.AppTheme
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import pillswaterreminder.shared.generated.resources.Res
import pillswaterreminder.shared.generated.resources.action_add_first
import pillswaterreminder.shared.generated.resources.action_allow
import pillswaterreminder.shared.generated.resources.action_get_started
import pillswaterreminder.shared.generated.resources.action_later
import pillswaterreminder.shared.generated.resources.action_open_settings
import pillswaterreminder.shared.generated.resources.action_turn_on
import pillswaterreminder.shared.generated.resources.onboarding_body
import pillswaterreminder.shared.generated.resources.onboarding_permissions_body
import pillswaterreminder.shared.generated.resources.onboarding_permissions_title
import pillswaterreminder.shared.generated.resources.onboarding_title
import pillswaterreminder.shared.generated.resources.perm_allowed
import pillswaterreminder.shared.generated.resources.perm_battery
import pillswaterreminder.shared.generated.resources.perm_battery_ok
import pillswaterreminder.shared.generated.resources.perm_battery_restricted
import pillswaterreminder.shared.generated.resources.perm_battery_tip
import pillswaterreminder.shared.generated.resources.perm_exact_alarms
import pillswaterreminder.shared.generated.resources.perm_notifications
import pillswaterreminder.shared.generated.resources.perm_off

@Composable
fun OnboardingScreen(
    onFinished: (addMedication: Boolean) -> Unit,
    viewModel: OnboardingViewModel = koinViewModel(),
) {
    var step by rememberSaveable { mutableStateOf(0) }
    val status by viewModel.permissionStatus.collectAsStateWithLifecycle()

    LifecycleResumeEffect(Unit) {
        viewModel.refresh()
        onPauseOrDispose { }
    }

    val requestNotifications = rememberNotificationPermissionRequest { granted ->
        if (!granted) viewModel.openNotificationSettings()
        viewModel.refresh()
    }

    if (step == 0) {
        WelcomeStep(onNext = { step = 1 })
    } else {
        PermissionsStep(
            status = status,
            onNotifications = requestNotifications,
            onExactAlarms = viewModel::openExactAlarmSettings,
            onBattery = viewModel::openBatterySettings,
            onAddFirst = { viewModel.finish { onFinished(true) } },
            onLater = { viewModel.finish { onFinished(false) } },
        )
    }
}

@Composable
private fun WelcomeStep(onNext: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(AppTheme.spacing.xxl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.l),
    ) {
        Spacer(Modifier.weight(1f))
        Text(
            stringResource(Res.string.onboarding_title),
            style = MaterialTheme.typography.displayLarge,
            textAlign = TextAlign.Center,
        )
        Text(
            stringResource(Res.string.onboarding_body),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.weight(1f))
        Button(onClick = onNext, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(Res.string.action_get_started))
        }
    }
}

@Composable
private fun PermissionsStep(
    status: PermissionStatus,
    onNotifications: () -> Unit,
    onExactAlarms: () -> Unit,
    onBattery: () -> Unit,
    onAddFirst: () -> Unit,
    onLater: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(AppTheme.spacing.xxl),
        verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.l),
    ) {
        Spacer(Modifier.weight(0.5f))
        Text(stringResource(Res.string.onboarding_permissions_title), style = MaterialTheme.typography.headlineMedium)
        Text(
            stringResource(Res.string.onboarding_permissions_body),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        ) {
            Column(Modifier.padding(AppTheme.spacing.l), verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.l)) {
                PermissionRow(
                    title = stringResource(Res.string.perm_notifications),
                    statusText = stringResource(if (status.notifications) Res.string.perm_allowed else Res.string.perm_off),
                    ok = status.notifications,
                    actionLabel = stringResource(Res.string.action_turn_on),
                    onAction = onNotifications,
                )
                PermissionRow(
                    title = stringResource(Res.string.perm_exact_alarms),
                    statusText = stringResource(if (status.exactAlarms) Res.string.perm_allowed else Res.string.perm_off),
                    ok = status.exactAlarms,
                    actionLabel = stringResource(Res.string.action_allow),
                    onAction = onExactAlarms,
                )
                PermissionRow(
                    title = stringResource(Res.string.perm_battery),
                    statusText = stringResource(if (status.batteryUnrestricted) Res.string.perm_battery_ok else Res.string.perm_battery_restricted),
                    ok = status.batteryUnrestricted,
                    actionLabel = stringResource(Res.string.action_open_settings),
                    onAction = onBattery,
                    detail = stringResource(Res.string.perm_battery_tip),
                )
            }
        }
        Spacer(Modifier.weight(1f))
        Button(
            onClick = onAddFirst,
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(stringResource(Res.string.action_add_first))
        }
        TextButton(onClick = onLater, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(Res.string.action_later))
        }
    }
}
