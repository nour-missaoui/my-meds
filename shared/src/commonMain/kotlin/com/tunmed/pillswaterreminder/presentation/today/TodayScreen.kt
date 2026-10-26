package com.tunmed.pillswaterreminder.presentation.today

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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tunmed.pillswaterreminder.domain.model.DoseStatus
import com.tunmed.pillswaterreminder.presentation.components.PlaceholderCard
import com.tunmed.pillswaterreminder.presentation.components.ScreenHeader
import com.tunmed.pillswaterreminder.presentation.theme.AppTheme
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import pillswaterreminder.shared.generated.resources.Res
import pillswaterreminder.shared.generated.resources.action_add_medication
import pillswaterreminder.shared.generated.resources.action_allow
import pillswaterreminder.shared.generated.resources.action_skip
import pillswaterreminder.shared.generated.resources.action_taken
import pillswaterreminder.shared.generated.resources.action_turn_on
import pillswaterreminder.shared.generated.resources.banner_exact_alarms_off
import pillswaterreminder.shared.generated.resources.banner_notifications_off
import pillswaterreminder.shared.generated.resources.status_due
import pillswaterreminder.shared.generated.resources.status_missed
import pillswaterreminder.shared.generated.resources.status_not_logged
import pillswaterreminder.shared.generated.resources.status_skipped
import pillswaterreminder.shared.generated.resources.status_snoozed
import pillswaterreminder.shared.generated.resources.status_taken_at
import pillswaterreminder.shared.generated.resources.status_upcoming
import pillswaterreminder.shared.generated.resources.title_today
import pillswaterreminder.shared.generated.resources.today_empty_body
import pillswaterreminder.shared.generated.resources.today_empty_title
import pillswaterreminder.shared.generated.resources.today_no_doses_body
import pillswaterreminder.shared.generated.resources.today_no_doses_title

@Composable
fun TodayScreen(
    onAddMedication: () -> Unit,
    viewModel: TodayViewModel = koinViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LifecycleResumeEffect(Unit) {
        viewModel.refresh()
        onPauseOrDispose { }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = AppTheme.spacing.l),
        verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.m),
    ) {
        ScreenHeader(title = stringResource(Res.string.title_today), overline = state.date?.toString())

        if (!state.notificationsAllowed) {
            PermissionBanner(
                text = stringResource(Res.string.banner_notifications_off),
                actionLabel = stringResource(Res.string.action_turn_on),
                onAction = viewModel::openNotificationSettings,
            )
        }
        if (!state.exactAlarmsAllowed) {
            PermissionBanner(
                text = stringResource(Res.string.banner_exact_alarms_off),
                actionLabel = stringResource(Res.string.action_allow),
                onAction = viewModel::openExactAlarmSettings,
            )
        }

        when {
            !state.hasMedications -> {
                PlaceholderCard(
                    title = stringResource(Res.string.today_empty_title),
                    body = stringResource(Res.string.today_empty_body),
                )
                Button(
                    onClick = onAddMedication,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(stringResource(Res.string.action_add_medication))
                }
            }
            state.rows.isEmpty() -> PlaceholderCard(
                title = stringResource(Res.string.today_no_doses_title),
                body = stringResource(Res.string.today_no_doses_body),
            )
            else -> state.rows.forEach { row ->
                key(row.key) {
                    DoseRow(row = row, onAnswer = { status -> viewModel.answer(row, status) })
                }
            }
        }
        Spacer(Modifier.height(AppTheme.spacing.xl))
    }
}

@Composable
private fun DoseRow(row: TodayDoseRow, onAnswer: (DoseStatus) -> Unit) {
    val (statusLabel, statusColor) = statusOf(row)
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Column(
            modifier = Modifier.padding(AppTheme.spacing.l),
            verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.s),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.m),
            ) {
                Text(row.time, style = MaterialTheme.typography.titleMedium)
                Column(Modifier.weight(1f)) {
                    Text(row.dose.medication.name, style = MaterialTheme.typography.titleMedium)
                    Text(
                        row.dose.doseText,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Text(statusLabel, style = MaterialTheme.typography.labelLarge, color = statusColor)
            }
            if (row.canAnswer) {
                Row(
                    modifier = Modifier.align(Alignment.End),
                    horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.s),
                ) {
                    OutlinedButton(onClick = { onAnswer(DoseStatus.SKIPPED) }) {
                        Text(stringResource(Res.string.action_skip))
                    }
                    Button(onClick = { onAnswer(DoseStatus.TAKEN) }) {
                        Text(stringResource(Res.string.action_taken))
                    }
                }
            }
        }
    }
}

@Composable
private fun statusOf(row: TodayDoseRow): Pair<String, Color> = when (row.dose.status) {
    DoseStatus.TAKEN -> stringResource(Res.string.status_taken_at, row.actedTime ?: row.time) to AppTheme.colors.taken
    DoseStatus.SKIPPED -> stringResource(Res.string.status_skipped) to AppTheme.colors.skipped
    DoseStatus.MISSED -> stringResource(Res.string.status_missed) to AppTheme.colors.missed
    DoseStatus.SNOOZED -> stringResource(Res.string.status_snoozed) to AppTheme.colors.due
    DoseStatus.PENDING -> stringResource(Res.string.status_due) to AppTheme.colors.due
    null ->
        if (row.isPast) {
            stringResource(Res.string.status_not_logged) to AppTheme.colors.textTertiary
        } else {
            stringResource(Res.string.status_upcoming) to MaterialTheme.colorScheme.onSurfaceVariant
        }
}

@Composable
private fun PermissionBanner(text: String, actionLabel: String, onAction: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = AppTheme.colors.missedContainer),
    ) {
        Row(
            modifier = Modifier.padding(start = AppTheme.spacing.l, end = AppTheme.spacing.s, top = AppTheme.spacing.s, bottom = AppTheme.spacing.s),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.s),
        ) {
            Text(text, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
            TextButton(onClick = onAction) { Text(actionLabel) }
        }
    }
}
