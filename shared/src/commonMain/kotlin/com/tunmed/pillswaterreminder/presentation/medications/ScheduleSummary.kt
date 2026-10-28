package com.tunmed.pillswaterreminder.presentation.medications

import androidx.compose.runtime.Composable
import com.tunmed.pillswaterreminder.core.time.toHhMm
import com.tunmed.pillswaterreminder.domain.model.MedSchedule
import com.tunmed.pillswaterreminder.domain.model.RecurrenceRule
import com.tunmed.pillswaterreminder.presentation.components.dayLabel
import com.tunmed.pillswaterreminder.presentation.components.monthRes
import com.tunmed.pillswaterreminder.presentation.components.weekdayRes
import com.tunmed.pillswaterreminder.presentation.components.weekdayShortRes
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.daysUntil
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource
import pillswaterreminder.shared.generated.resources.Res
import pillswaterreminder.shared.generated.resources.every_n_days
import pillswaterreminder.shared.generated.resources.next_on
import pillswaterreminder.shared.generated.resources.next_today
import pillswaterreminder.shared.generated.resources.next_tomorrow
import pillswaterreminder.shared.generated.resources.summary_monthly_day
import pillswaterreminder.shared.generated.resources.summary_monthly_weekday
import pillswaterreminder.shared.generated.resources.summary_weekly
import pillswaterreminder.shared.generated.resources.summary_yearly

@Composable
fun scheduleSummary(schedule: MedSchedule): String {
    val rule = schedule.rule
    val repeat = when (rule) {
        RecurrenceRule.Daily -> repeatLabel(RepeatKind.DAILY)
        is RecurrenceRule.EveryNDays -> pluralStringResource(Res.plurals.every_n_days, rule.interval, rule.interval)
        is RecurrenceRule.Weekly -> {
            val days = rule.days.sortedBy { it.ordinal }.map { stringResource(weekdayShortRes(it)) }
            stringResource(Res.string.summary_weekly, days.joinToString(", "))
        }
        is RecurrenceRule.MonthlyOnDay -> stringResource(Res.string.summary_monthly_day, rule.dayOfMonth)
        is RecurrenceRule.MonthlyOnWeekday -> stringResource(
            Res.string.summary_monthly_weekday,
            stringResource(weekOfMonthRes(rule.weekOfMonth)),
            stringResource(weekdayRes(rule.dayOfWeek)),
        )
        is RecurrenceRule.Yearly -> stringResource(Res.string.summary_yearly, rule.dayOfMonth, stringResource(monthRes(rule.month)))
        RecurrenceRule.AsNeeded -> repeatLabel(RepeatKind.AS_NEEDED)
    }
    if (rule == RecurrenceRule.AsNeeded || schedule.times.isEmpty()) return repeat
    return "$repeat · ${schedule.times.sorted().joinToString(", ") { it.toHhMm() }}"
}

@Composable
fun nextDoseLabel(next: LocalDateTime, today: LocalDate): String {
    val time = next.time.toHhMm()
    return when (today.daysUntil(next.date)) {
        0 -> stringResource(Res.string.next_today, time)
        1 -> stringResource(Res.string.next_tomorrow, time)
        else -> stringResource(Res.string.next_on, dayLabel(next.date), time)
    }
}
