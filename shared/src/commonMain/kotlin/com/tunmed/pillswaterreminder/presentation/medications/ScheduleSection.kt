package com.tunmed.pillswaterreminder.presentation.medications

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.tunmed.pillswaterreminder.domain.model.RecurrenceRule
import com.tunmed.pillswaterreminder.presentation.components.SelectField
import com.tunmed.pillswaterreminder.presentation.components.monthRes
import com.tunmed.pillswaterreminder.presentation.components.weekdayRes
import com.tunmed.pillswaterreminder.presentation.components.weekdayShortRes
import com.tunmed.pillswaterreminder.presentation.theme.AppTheme
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.Month
import kotlinx.datetime.isoDayNumber
import kotlinx.datetime.number
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource
import pillswaterreminder.shared.generated.resources.Res
import pillswaterreminder.shared.generated.resources.as_needed_note
import pillswaterreminder.shared.generated.resources.cd_fewer_days
import pillswaterreminder.shared.generated.resources.cd_more_days
import pillswaterreminder.shared.generated.resources.day_clamp_note
import pillswaterreminder.shared.generated.resources.every_n_days
import pillswaterreminder.shared.generated.resources.every_n_days_note
import pillswaterreminder.shared.generated.resources.feb29_note
import pillswaterreminder.shared.generated.resources.heads_up_days
import pillswaterreminder.shared.generated.resources.heads_up_label
import pillswaterreminder.shared.generated.resources.heads_up_off
import pillswaterreminder.shared.generated.resources.ic_add
import pillswaterreminder.shared.generated.resources.ic_remove
import pillswaterreminder.shared.generated.resources.label_day
import pillswaterreminder.shared.generated.resources.label_month
import pillswaterreminder.shared.generated.resources.label_weekday
import pillswaterreminder.shared.generated.resources.label_which
import pillswaterreminder.shared.generated.resources.monthly_on_day
import pillswaterreminder.shared.generated.resources.monthly_on_weekday
import pillswaterreminder.shared.generated.resources.repeat_as_needed
import pillswaterreminder.shared.generated.resources.repeat_daily
import pillswaterreminder.shared.generated.resources.repeat_every_n_days
import pillswaterreminder.shared.generated.resources.repeat_label
import pillswaterreminder.shared.generated.resources.repeat_monthly
import pillswaterreminder.shared.generated.resources.repeat_weekly
import pillswaterreminder.shared.generated.resources.repeat_yearly
import pillswaterreminder.shared.generated.resources.week_1
import pillswaterreminder.shared.generated.resources.week_2
import pillswaterreminder.shared.generated.resources.week_3
import pillswaterreminder.shared.generated.resources.week_4
import pillswaterreminder.shared.generated.resources.week_last
import pillswaterreminder.shared.generated.resources.weekdays_error

private val WEEKDAYS: List<DayOfWeek> = (1..7).map { DayOfWeek(it) }
private val MONTHS: List<Month> = (1..12).map { Month(it) }
private val WEEKS_OF_MONTH = listOf(1, 2, 3, 4, RecurrenceRule.MonthlyOnWeekday.LAST)
private const val WEEKDAY_CHIPS_PER_ROW = 4

@Composable
fun ScheduleSection(draft: ScheduleDraft, onChange: (ScheduleDraft) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.m)) {
        SelectField(
            label = stringResource(Res.string.repeat_label),
            value = repeatLabel(draft.kind),
            options = RepeatKind.entries,
            optionLabel = { repeatLabel(it) },
            onSelect = { kind -> onChange(draft.copy(kind = kind)) },
            modifier = Modifier.fillMaxWidth(),
        )
        when (draft.kind) {
            RepeatKind.DAILY -> Unit

            RepeatKind.EVERY_N_DAYS -> {
                NumberStepper(
                    label = pluralStringResource(Res.plurals.every_n_days, draft.everyNDays, draft.everyNDays),
                    value = draft.everyNDays,
                    range = ScheduleDraft.MIN_EVERY_N_DAYS..ScheduleDraft.MAX_EVERY_N_DAYS,
                    onChange = { onChange(draft.copy(everyNDays = it)) },
                    decreaseLabel = stringResource(Res.string.cd_fewer_days),
                    increaseLabel = stringResource(Res.string.cd_more_days),
                )
                Note(stringResource(Res.string.every_n_days_note))
            }

            RepeatKind.WEEKLY -> {
                WEEKDAYS.chunked(WEEKDAY_CHIPS_PER_ROW).forEach { row ->
                    Row(horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.s)) {
                        row.forEach { day ->
                            val selected = day in draft.weekdays
                            FilterChip(
                                selected = selected,
                                onClick = {
                                    onChange(draft.copy(weekdays = if (selected) draft.weekdays - day else draft.weekdays + day))
                                },
                                label = { Text(stringResource(weekdayShortRes(day))) },
                                modifier = Modifier.weight(1f),
                            )
                        }
                        repeat(WEEKDAY_CHIPS_PER_ROW - row.size) { Spacer(Modifier.weight(1f)) }
                    }
                }
                if (draft.weekdays.isEmpty()) Note(stringResource(Res.string.weekdays_error), error = true)
            }

            RepeatKind.MONTHLY -> {
                Row(horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.s)) {
                    FilterChip(
                        selected = !draft.monthlyByWeekday,
                        onClick = { onChange(draft.copy(monthlyByWeekday = false)) },
                        label = { Text(stringResource(Res.string.monthly_on_day)) },
                    )
                    FilterChip(
                        selected = draft.monthlyByWeekday,
                        onClick = { onChange(draft.copy(monthlyByWeekday = true)) },
                        label = { Text(stringResource(Res.string.monthly_on_weekday)) },
                    )
                }
                if (draft.monthlyByWeekday) {
                    Row(horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.m)) {
                        SelectField(
                            label = stringResource(Res.string.label_which),
                            value = stringResource(weekOfMonthRes(draft.weekOfMonth)),
                            options = WEEKS_OF_MONTH,
                            optionLabel = { stringResource(weekOfMonthRes(it)) },
                            onSelect = { onChange(draft.copy(weekOfMonth = it)) },
                            modifier = Modifier.weight(1f),
                        )
                        SelectField(
                            label = stringResource(Res.string.label_weekday),
                            value = stringResource(weekdayRes(draft.weekday)),
                            options = WEEKDAYS,
                            optionLabel = { stringResource(weekdayRes(it)) },
                            onSelect = { onChange(draft.copy(weekday = it)) },
                            modifier = Modifier.weight(1.4f),
                        )
                    }
                } else {
                    SelectField(
                        label = stringResource(Res.string.label_day),
                        value = "${draft.dayOfMonth}",
                        options = (1..31).toList(),
                        optionLabel = { "$it" },
                        onSelect = { onChange(draft.copy(dayOfMonth = it)) },
                        modifier = Modifier.fillMaxWidth(),
                    )
                    if (draft.dayOfMonth >= 29) Note(stringResource(Res.string.day_clamp_note, draft.dayOfMonth))
                }
                HeadsUpField(draft, onChange)
            }

            RepeatKind.YEARLY -> {
                val maxDay = ScheduleDraft.maxDayOf(draft.month)
                val day = draft.dayOfMonth.coerceAtMost(maxDay)
                Row(horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.m)) {
                    SelectField(
                        label = stringResource(Res.string.label_month),
                        value = stringResource(monthRes(draft.month)),
                        options = MONTHS,
                        optionLabel = { stringResource(monthRes(it)) },
                        onSelect = { month ->
                            onChange(draft.copy(month = month, dayOfMonth = draft.dayOfMonth.coerceAtMost(ScheduleDraft.maxDayOf(month))))
                        },
                        modifier = Modifier.weight(1.4f),
                    )
                    SelectField(
                        label = stringResource(Res.string.label_day),
                        value = "$day",
                        options = (1..maxDay).toList(),
                        optionLabel = { "$it" },
                        onSelect = { onChange(draft.copy(dayOfMonth = it)) },
                        modifier = Modifier.weight(1f),
                    )
                }
                if (draft.month == Month.FEBRUARY && day == 29) Note(stringResource(Res.string.feb29_note))
                HeadsUpField(draft, onChange)
            }

            RepeatKind.AS_NEEDED -> Note(stringResource(Res.string.as_needed_note))
        }
    }
}

val ScheduleDraftSaver: Saver<ScheduleDraft, Any> = listSaver(
    save = { draft ->
        listOf(
            draft.kind.name,
            draft.everyNDays,
            draft.anchor.toString(),
            draft.weekdays.map { it.isoDayNumber }.sorted().joinToString(","),
            draft.monthlyByWeekday,
            draft.dayOfMonth,
            draft.weekOfMonth,
            draft.weekday.isoDayNumber,
            draft.month.number,
            draft.headsUpDays,
        )
    },
    restore = { values ->
        ScheduleDraft(
            kind = RepeatKind.valueOf(values[0] as String),
            everyNDays = values[1] as Int,
            anchor = LocalDate.parse(values[2] as String),
            weekdays = (values[3] as String).split(',').filter { it.isNotBlank() }.map { DayOfWeek(it.toInt()) }.toSet(),
            monthlyByWeekday = values[4] as Boolean,
            dayOfMonth = values[5] as Int,
            weekOfMonth = values[6] as Int,
            weekday = DayOfWeek(values[7] as Int),
            month = Month(values[8] as Int),
            headsUpDays = values[9] as Int,
        )
    },
)

@Composable
private fun HeadsUpField(draft: ScheduleDraft, onChange: (ScheduleDraft) -> Unit) {
    SelectField(
        label = stringResource(Res.string.heads_up_label),
        value = headsUpLabel(draft.headsUpDays),
        options = ScheduleDraft.HEADS_UP_CHOICES,
        optionLabel = { headsUpLabel(it) },
        onSelect = { onChange(draft.copy(headsUpDays = it)) },
        modifier = Modifier.fillMaxWidth(),
    )
}

@Composable
private fun NumberStepper(
    label: String,
    value: Int,
    range: IntRange,
    onChange: (Int) -> Unit,
    decreaseLabel: String,
    increaseLabel: String,
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(label, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
        IconButton(onClick = { onChange(value - 1) }, enabled = value > range.first) {
            Icon(painterResource(Res.drawable.ic_remove), contentDescription = decreaseLabel)
        }
        Text("$value", style = MaterialTheme.typography.titleMedium)
        IconButton(onClick = { onChange(value + 1) }, enabled = value < range.last) {
            Icon(painterResource(Res.drawable.ic_add), contentDescription = increaseLabel)
        }
    }
}

@Composable
private fun Note(text: String, error: Boolean = false) {
    Text(
        text,
        style = MaterialTheme.typography.bodySmall,
        color = if (error) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

@Composable
internal fun repeatLabel(kind: RepeatKind): String = stringResource(
    when (kind) {
        RepeatKind.DAILY -> Res.string.repeat_daily
        RepeatKind.EVERY_N_DAYS -> Res.string.repeat_every_n_days
        RepeatKind.WEEKLY -> Res.string.repeat_weekly
        RepeatKind.MONTHLY -> Res.string.repeat_monthly
        RepeatKind.YEARLY -> Res.string.repeat_yearly
        RepeatKind.AS_NEEDED -> Res.string.repeat_as_needed
    },
)

@Composable
private fun headsUpLabel(days: Int): String =
    if (days == 0) stringResource(Res.string.heads_up_off) else pluralStringResource(Res.plurals.heads_up_days, days, days)

internal fun weekOfMonthRes(week: Int): StringResource = when (week) {
    1 -> Res.string.week_1
    2 -> Res.string.week_2
    3 -> Res.string.week_3
    4 -> Res.string.week_4
    else -> Res.string.week_last
}
