package com.tunmed.pillswaterreminder.presentation.history

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tunmed.pillswaterreminder.core.time.toHhMm
import com.tunmed.pillswaterreminder.domain.model.DayStatus
import com.tunmed.pillswaterreminder.domain.model.DoseStatus
import com.tunmed.pillswaterreminder.domain.model.HistoryEntry
import com.tunmed.pillswaterreminder.domain.model.HistoryMonth
import com.tunmed.pillswaterreminder.domain.model.MedicationAdherence
import com.tunmed.pillswaterreminder.presentation.components.ColorDot
import com.tunmed.pillswaterreminder.presentation.components.ScreenHeader
import com.tunmed.pillswaterreminder.presentation.components.StatusChip
import com.tunmed.pillswaterreminder.presentation.components.dayLabel
import com.tunmed.pillswaterreminder.presentation.components.doseStatusStyle
import com.tunmed.pillswaterreminder.presentation.components.monthRes
import com.tunmed.pillswaterreminder.presentation.components.weekdayShortRes
import com.tunmed.pillswaterreminder.presentation.theme.AppTheme
import com.tunmed.pillswaterreminder.presentation.theme.toColor
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.isoDayNumber
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import pillswaterreminder.shared.generated.resources.Res
import pillswaterreminder.shared.generated.resources.action_change
import pillswaterreminder.shared.generated.resources.adherence_detail
import pillswaterreminder.shared.generated.resources.cd_next_month
import pillswaterreminder.shared.generated.resources.cd_previous_month
import pillswaterreminder.shared.generated.resources.history_day_empty
import pillswaterreminder.shared.generated.resources.history_no_data
import pillswaterreminder.shared.generated.resources.history_overall
import pillswaterreminder.shared.generated.resources.history_this_month
import pillswaterreminder.shared.generated.resources.ic_chevron_left
import pillswaterreminder.shared.generated.resources.ic_chevron_right
import pillswaterreminder.shared.generated.resources.legend_all_taken
import pillswaterreminder.shared.generated.resources.legend_missed
import pillswaterreminder.shared.generated.resources.legend_partial
import pillswaterreminder.shared.generated.resources.month_year
import pillswaterreminder.shared.generated.resources.status_missed
import pillswaterreminder.shared.generated.resources.status_skipped
import pillswaterreminder.shared.generated.resources.status_taken_at
import pillswaterreminder.shared.generated.resources.title_history

@Composable
fun HistoryScreen(
    viewModel: HistoryViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LifecycleResumeEffect(Unit) {
        viewModel.refresh()
        onPauseOrDispose { }
    }

    val month = state.month
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = AppTheme.spacing.l),
        verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.m),
    ) {
        ScreenHeader(
            title = stringResource(Res.string.title_history),
            overline = month?.overallPercent?.let { stringResource(Res.string.history_overall, it) },
        )
        if (month != null) {
            MonthBar(
                first = month.firstDay,
                canGoNext = state.canGoNext,
                onPrevious = viewModel::previousMonth,
                onNext = viewModel::nextMonth,
            )
            CalendarCard(month = month, selected = state.selected, today = state.today, onSelect = viewModel::select)
            state.selected?.let { day ->
                DayCard(day = day, entries = month.entriesByDay[day].orEmpty(), onCorrect = viewModel::correct)
            }
            AdherenceCard(month.adherence)
        }
        Spacer(Modifier.height(AppTheme.spacing.xl))
    }
}

@Composable
private fun MonthBar(first: LocalDate, canGoNext: Boolean, onPrevious: () -> Unit, onNext: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        IconButton(onClick = onPrevious) {
            Icon(painterResource(Res.drawable.ic_chevron_left), contentDescription = stringResource(Res.string.cd_previous_month))
        }
        Text(
            stringResource(Res.string.month_year, stringResource(monthRes(first.month)), first.year),
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center,
            modifier = Modifier.weight(1f),
        )
        IconButton(onClick = onNext, enabled = canGoNext) {
            Icon(painterResource(Res.drawable.ic_chevron_right), contentDescription = stringResource(Res.string.cd_next_month))
        }
    }
}

@Composable
private fun CalendarCard(month: HistoryMonth, selected: LocalDate?, today: LocalDate?, onSelect: (LocalDate) -> Unit) {
    val first = month.firstDay
    val length = first.plus(1, DateTimeUnit.MONTH).minus(1, DateTimeUnit.DAY).day
    val leading = first.dayOfWeek.isoDayNumber - 1
    val cells: List<LocalDate?> = List(leading) { null } + (1..length).map { LocalDate(first.year, first.month, it) }
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Column(Modifier.padding(AppTheme.spacing.m), verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.xs)) {
            Row {
                (1..7).forEach { iso ->
                    Text(
                        stringResource(weekdayShortRes(DayOfWeek(iso))),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.weight(1f),
                    )
                }
            }
            cells.chunked(7).forEach { week ->
                Row {
                    week.forEach { date ->
                        Box(Modifier.weight(1f).aspectRatio(1f), contentAlignment = Alignment.Center) {
                            if (date != null) {
                                DayCell(
                                    date = date,
                                    status = month.dayStatus[date] ?: DayStatus.NONE,
                                    selected = date == selected,
                                    isToday = date == today,
                                    enabled = today == null || date <= today,
                                    onClick = { onSelect(date) },
                                )
                            }
                        }
                    }
                    repeat(7 - week.size) { Spacer(Modifier.weight(1f)) }
                }
            }
            Legend()
            if (month.entriesByDay.isEmpty()) {
                Text(
                    stringResource(Res.string.history_no_data),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun DayCell(date: LocalDate, status: DayStatus, selected: Boolean, isToday: Boolean, enabled: Boolean, onClick: () -> Unit) {
    val background = if (selected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent
    Column(
        modifier = Modifier
            .padding(2.dp)
            .clip(CircleShape)
            .background(background)
            .then(if (isToday) Modifier.border(1.dp, MaterialTheme.colorScheme.primary, CircleShape) else Modifier)
            .clickable(enabled = enabled, onClick = onClick)
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            "${date.day}",
            style = MaterialTheme.typography.bodyMedium,
            color = if (enabled) MaterialTheme.colorScheme.onSurface else AppTheme.colors.textTertiary,
        )
        val dot = dayStatusColor(status)
        Box(
            Modifier
                .padding(top = 2.dp)
                .size(6.dp)
                .clip(CircleShape)
                .background(dot ?: Color.Transparent),
        )
    }
}

@Composable
private fun dayStatusColor(status: DayStatus): Color? = when (status) {
    DayStatus.ALL_TAKEN -> AppTheme.colors.taken
    DayStatus.PARTIAL -> AppTheme.colors.due
    DayStatus.MISSED -> AppTheme.colors.missed
    DayStatus.IN_PROGRESS -> MaterialTheme.colorScheme.primary
    DayStatus.NONE -> null
}

@Composable
private fun Legend() {
    Row(
        horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.l),
        modifier = Modifier.padding(top = AppTheme.spacing.s),
    ) {
        LegendItem(AppTheme.colors.taken, stringResource(Res.string.legend_all_taken))
        LegendItem(AppTheme.colors.due, stringResource(Res.string.legend_partial))
        LegendItem(AppTheme.colors.missed, stringResource(Res.string.legend_missed))
    }
}

@Composable
private fun LegendItem(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(8.dp).clip(CircleShape).background(color))
        Spacer(Modifier.width(AppTheme.spacing.xs))
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun DayCard(day: LocalDate, entries: List<HistoryEntry>, onCorrect: (HistoryEntry, DoseStatus) -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Column(Modifier.padding(AppTheme.spacing.l), verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.m)) {
            Text(dayLabel(day), style = MaterialTheme.typography.titleSmall)
            if (entries.isEmpty()) {
                Text(
                    stringResource(Res.string.history_day_empty),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            entries.forEach { entry -> EntryRow(entry, onCorrect) }
        }
    }
}

@Composable
private fun EntryRow(entry: HistoryEntry, onCorrect: (HistoryEntry, DoseStatus) -> Unit) {
    val time = entry.time.toHhMm()
    val actedTime = entry.log.actedAt?.toLocalDateTime(TimeZone.currentSystemDefault())?.time?.toHhMm()
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(time, style = MaterialTheme.typography.labelLarge, modifier = Modifier.width(48.dp))
        ColorDot(entry.medication.color.toColor())
        Column(Modifier.weight(1f)) {
            Text(entry.medication.name, style = MaterialTheme.typography.bodyLarge)
            StatusChip(doseStatusStyle(entry.log.status, actedTime, time, isPast = true))
        }
        if (!entry.asNeeded && entry.log.status != DoseStatus.PENDING && entry.log.status != DoseStatus.SNOOZED) {
            CorrectionMenu(entry, onCorrect)
        }
    }
}

@Composable
private fun CorrectionMenu(entry: HistoryEntry, onCorrect: (HistoryEntry, DoseStatus) -> Unit) {
    var open by remember { mutableStateOf(false) }
    Box {
        TextButton(onClick = { open = true }) { Text(stringResource(Res.string.action_change)) }
        DropdownMenu(expanded = open, onDismissRequest = { open = false }) {
            listOf(
                DoseStatus.TAKEN to stringResource(Res.string.status_taken_at, entry.time.toHhMm()),
                DoseStatus.SKIPPED to stringResource(Res.string.status_skipped),
                DoseStatus.MISSED to stringResource(Res.string.status_missed),
            ).filter { it.first != entry.log.status }.forEach { (status, label) ->
                DropdownMenuItem(
                    text = { Text(label) },
                    onClick = {
                        open = false
                        onCorrect(entry, status)
                    },
                )
            }
        }
    }
}

@Composable
private fun AdherenceCard(adherence: List<MedicationAdherence>) {
    if (adherence.isEmpty()) return
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Column(Modifier.padding(AppTheme.spacing.l), verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.m)) {
            Text(stringResource(Res.string.history_this_month), style = MaterialTheme.typography.titleSmall)
            adherence.forEach { item ->
                val percent = item.percent ?: 0
                Column(verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.xs)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        ColorDot(item.medication.color.toColor())
                        Text(item.medication.name, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
                        Text(
                            stringResource(Res.string.adherence_detail, percent, item.taken, item.answered),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    LinearProgressIndicator(
                        progress = { percent / 100f },
                        modifier = Modifier.fillMaxWidth(),
                        color = if (percent >= 80) AppTheme.colors.taken else if (percent >= 50) AppTheme.colors.due else AppTheme.colors.missed,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant,
                    )
                }
            }
        }
    }
}
