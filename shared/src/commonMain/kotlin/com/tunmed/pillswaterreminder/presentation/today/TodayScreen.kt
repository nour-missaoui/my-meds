package com.tunmed.pillswaterreminder.presentation.today

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tunmed.pillswaterreminder.core.time.toHhMm
import com.tunmed.pillswaterreminder.domain.model.DoseStatus
import com.tunmed.pillswaterreminder.domain.reminder.UpcomingDose
import com.tunmed.pillswaterreminder.presentation.components.ColorDot
import com.tunmed.pillswaterreminder.presentation.components.PlaceholderCard
import com.tunmed.pillswaterreminder.presentation.components.ScreenHeader
import com.tunmed.pillswaterreminder.presentation.components.StatusChip
import com.tunmed.pillswaterreminder.presentation.components.dayLabel
import com.tunmed.pillswaterreminder.presentation.components.doseStatusStyle
import com.tunmed.pillswaterreminder.presentation.components.longDayLabel
import com.tunmed.pillswaterreminder.presentation.permissions.rememberNotificationPermissionRequest
import com.tunmed.pillswaterreminder.presentation.theme.AppTheme
import com.tunmed.pillswaterreminder.presentation.theme.toColor
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import pillswaterreminder.shared.generated.resources.Res
import pillswaterreminder.shared.generated.resources.action_add_medication
import pillswaterreminder.shared.generated.resources.action_allow
import pillswaterreminder.shared.generated.resources.action_log_dose
import pillswaterreminder.shared.generated.resources.action_skip
import pillswaterreminder.shared.generated.resources.action_taken
import pillswaterreminder.shared.generated.resources.action_turn_on
import pillswaterreminder.shared.generated.resources.as_needed_none
import pillswaterreminder.shared.generated.resources.as_needed_taken
import pillswaterreminder.shared.generated.resources.banner_exact_alarms_off
import pillswaterreminder.shared.generated.resources.banner_notifications_off
import pillswaterreminder.shared.generated.resources.section_upcoming
import pillswaterreminder.shared.generated.resources.section_when_needed
import pillswaterreminder.shared.generated.resources.title_today
import pillswaterreminder.shared.generated.resources.today_empty_body
import pillswaterreminder.shared.generated.resources.today_empty_title
import pillswaterreminder.shared.generated.resources.today_no_doses_body
import pillswaterreminder.shared.generated.resources.today_no_doses_title
import pillswaterreminder.shared.generated.resources.today_progress
import pillswaterreminder.shared.generated.resources.upcoming_in

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

    val requestNotifications = rememberNotificationPermissionRequest { granted ->
        if (!granted) viewModel.openNotificationSettings()
        viewModel.refresh()
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = AppTheme.spacing.l, end = AppTheme.spacing.l, bottom = AppTheme.spacing.xxl),
        verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.m),
    ) {
        item(key = "header") {
            ScreenHeader(title = stringResource(Res.string.title_today), overline = state.date?.let { longDayLabel(it) })
        }
        if (!state.notificationsAllowed) {
            item(key = "banner-notifications") {
                PermissionBanner(
                    text = stringResource(Res.string.banner_notifications_off),
                    actionLabel = stringResource(Res.string.action_turn_on),
                    onAction = requestNotifications,
                )
            }
        }
        if (!state.exactAlarmsAllowed) {
            item(key = "banner-exact") {
                PermissionBanner(
                    text = stringResource(Res.string.banner_exact_alarms_off),
                    actionLabel = stringResource(Res.string.action_allow),
                    onAction = viewModel::openExactAlarmSettings,
                )
            }
        }
        if (state.loaded && !state.hasMedications) {
            item(key = "empty") {
                Column(verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.m)) {
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
            }
        } else if (state.loaded) {
            if (state.rows.isNotEmpty()) {
                item(key = "progress") { ProgressCard(taken = state.takenCount, total = state.rows.size) }
            } else if (state.asNeeded.isEmpty()) {
                item(key = "no-doses") {
                    PlaceholderCard(
                        title = stringResource(Res.string.today_no_doses_title),
                        body = stringResource(Res.string.today_no_doses_body),
                    )
                }
            }
            state.timeGroups.forEach { (time, rows) ->
                item(key = "time-$time") {
                    TimeGroup(time = time, rows = rows, onAnswer = viewModel::answer)
                }
            }
            if (state.asNeeded.isNotEmpty()) {
                item(key = "as-needed-title") { SectionTitle(stringResource(Res.string.section_when_needed)) }
                items(state.asNeeded, key = { "as-needed-${it.item.medication.id}" }) { row ->
                    AsNeededCard(row = row, onLog = { viewModel.logAsNeeded(row.item.medication.id) })
                }
            }
            if (state.upcoming.isNotEmpty()) {
                item(key = "upcoming") { UpcomingCard(state.upcoming) }
            }
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(top = AppTheme.spacing.s),
    )
}

@Composable
private fun ProgressCard(taken: Int, total: Int) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
    ) {
        Column(Modifier.padding(AppTheme.spacing.l), verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.s)) {
            Text(
                pluralStringResource(Res.plurals.today_progress, total, taken, total),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )
            LinearProgressIndicator(
                progress = { if (total == 0) 0f else taken.toFloat() / total },
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surface,
            )
        }
    }
}

@Composable
private fun TimeGroup(time: String, rows: List<TodayDoseRow>, onAnswer: (TodayDoseRow, DoseStatus) -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.m)) {
        Text(
            time,
            style = MaterialTheme.typography.titleSmall,
            modifier = Modifier.width(52.dp).padding(top = AppTheme.spacing.l),
        )
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.s)) {
            rows.forEach { row -> DoseCard(row = row, onAnswer = { status -> onAnswer(row, status) }) }
        }
    }
}

@Composable
private fun DoseCard(row: TodayDoseRow, onAnswer: (DoseStatus) -> Unit) {
    val style = doseStatusStyle(row.dose.status, row.actedTime, row.time, row.isPast)
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Column(Modifier.padding(AppTheme.spacing.l), verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.s)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                ColorDot(row.dose.medication.color.toColor())
                Column(Modifier.weight(1f)) {
                    Text(row.dose.medication.name, style = MaterialTheme.typography.titleMedium)
                    Text(
                        row.dose.doseText,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                StatusChip(style)
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
private fun AsNeededCard(row: AsNeededRow, onLog: () -> Unit) {
    val medication = row.item.medication
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Row(Modifier.padding(AppTheme.spacing.l), verticalAlignment = Alignment.CenterVertically) {
            ColorDot(medication.color.toColor())
            Column(Modifier.weight(1f)) {
                Text(medication.name, style = MaterialTheme.typography.titleMedium)
                Text(medication.dose, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(
                    if (row.item.takenCount > 0 && row.lastTime != null) {
                        pluralStringResource(Res.plurals.as_needed_taken, row.item.takenCount, row.item.takenCount, row.lastTime)
                    } else {
                        stringResource(Res.string.as_needed_none)
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            FilledTonalButton(onClick = onLog) { Text(stringResource(Res.string.action_log_dose)) }
        }
    }
}

@Composable
private fun UpcomingCard(upcoming: List<UpcomingDose>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Column(Modifier.padding(AppTheme.spacing.l), verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.m)) {
            Text(stringResource(Res.string.section_upcoming), style = MaterialTheme.typography.titleSmall)
            upcoming.forEach { dose ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    ColorDot(dose.medication.color.toColor())
                    Column(Modifier.weight(1f)) {
                        Text(dose.medication.name, style = MaterialTheme.typography.bodyLarge)
                        Text(
                            "${dayLabel(dose.dueAt.date)}, ${dose.dueAt.time.toHhMm()}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Text(
                        pluralStringResource(Res.plurals.upcoming_in, dose.daysAhead, dose.daysAhead),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
            }
        }
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
