package com.tunmed.pillswaterreminder.presentation.medications

import com.tunmed.pillswaterreminder.domain.model.RecurrenceRule
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.Month

enum class RepeatKind {
    DAILY, EVERY_N_DAYS, WEEKLY, MONTHLY, YEARLY, AS_NEEDED;

    val hasHeadsUp: Boolean get() = this == MONTHLY || this == YEARLY

    val hasTimes: Boolean get() = this != AS_NEEDED
}

data class ScheduleDraft(
    val kind: RepeatKind,
    val everyNDays: Int,
    val anchor: LocalDate,
    val weekdays: Set<DayOfWeek>,
    val monthlyByWeekday: Boolean,
    val dayOfMonth: Int,
    val weekOfMonth: Int,
    val weekday: DayOfWeek,
    val month: Month,
    val headsUpDays: Int,
) {
    fun toRule(): RecurrenceRule? = when (kind) {
        RepeatKind.DAILY -> RecurrenceRule.Daily
        RepeatKind.EVERY_N_DAYS -> RecurrenceRule.EveryNDays(interval = everyNDays, anchor = anchor)
        RepeatKind.WEEKLY -> weekdays.takeIf { it.isNotEmpty() }?.let { RecurrenceRule.Weekly(it) }
        RepeatKind.MONTHLY ->
            if (monthlyByWeekday) {
                RecurrenceRule.MonthlyOnWeekday(weekOfMonth = weekOfMonth, dayOfWeek = weekday)
            } else {
                RecurrenceRule.MonthlyOnDay(dayOfMonth)
            }
        RepeatKind.YEARLY -> RecurrenceRule.Yearly(month = month, dayOfMonth = dayOfMonth.coerceAtMost(maxDayOf(month)))
        RepeatKind.AS_NEEDED -> RecurrenceRule.AsNeeded
    }

    fun headsUpToSave(): Int = if (kind.hasHeadsUp) headsUpDays else 0

    companion object {
        const val MIN_EVERY_N_DAYS = 2
        const val MAX_EVERY_N_DAYS = 365
        val HEADS_UP_CHOICES = listOf(0, 1, 2, 3, 7)
        const val DEFAULT_HEADS_UP = 1

        fun startingOn(today: LocalDate): ScheduleDraft = ScheduleDraft(
            kind = RepeatKind.DAILY,
            everyNDays = MIN_EVERY_N_DAYS,
            anchor = today,
            weekdays = setOf(today.dayOfWeek),
            monthlyByWeekday = false,
            dayOfMonth = today.day,
            weekOfMonth = ((today.day - 1) / 7 + 1).coerceAtMost(4),
            weekday = today.dayOfWeek,
            month = today.month,
            headsUpDays = DEFAULT_HEADS_UP,
        )

        fun of(rule: RecurrenceRule, headsUpDays: Int, today: LocalDate): ScheduleDraft {
            val base = startingOn(today)
            return when (rule) {
                RecurrenceRule.Daily -> base
                is RecurrenceRule.EveryNDays ->
                    base.copy(kind = RepeatKind.EVERY_N_DAYS, everyNDays = rule.interval, anchor = rule.anchor)
                is RecurrenceRule.Weekly -> base.copy(kind = RepeatKind.WEEKLY, weekdays = rule.days)
                is RecurrenceRule.MonthlyOnDay ->
                    base.copy(kind = RepeatKind.MONTHLY, dayOfMonth = rule.dayOfMonth, headsUpDays = headsUpDays)
                is RecurrenceRule.MonthlyOnWeekday -> base.copy(
                    kind = RepeatKind.MONTHLY,
                    monthlyByWeekday = true,
                    weekOfMonth = rule.weekOfMonth,
                    weekday = rule.dayOfWeek,
                    headsUpDays = headsUpDays,
                )
                is RecurrenceRule.Yearly -> base.copy(
                    kind = RepeatKind.YEARLY,
                    month = rule.month,
                    dayOfMonth = rule.dayOfMonth,
                    headsUpDays = headsUpDays,
                )
                RecurrenceRule.AsNeeded -> base.copy(kind = RepeatKind.AS_NEEDED)
            }
        }

        fun maxDayOf(month: Month): Int = when (month) {
            Month.FEBRUARY -> 29
            Month.APRIL, Month.JUNE, Month.SEPTEMBER, Month.NOVEMBER -> 30
            else -> 31
        }
    }
}
