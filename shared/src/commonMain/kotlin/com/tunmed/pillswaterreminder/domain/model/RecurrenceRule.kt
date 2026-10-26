package com.tunmed.pillswaterreminder.domain.model

import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.Month

/**
 * When a medication is due. Times of day live on [MedSchedule.times]; the rule only
 * decides on which dates the medication is due.
 *
 * Edge cases (implemented in ScheduleEngine):
 * - MonthlyOnDay(29..31) falls on the last day of shorter months.
 * - Yearly(FEBRUARY, 29) falls on 28 February in non-leap years.
 * - EveryNDays counts from [EveryNDays.anchor], not from the last dose taken.
 */
sealed interface RecurrenceRule {
    data object Daily : RecurrenceRule

    data class EveryNDays(val interval: Int, val anchor: LocalDate) : RecurrenceRule

    data class Weekly(val days: Set<DayOfWeek>) : RecurrenceRule

    /** [dayOfMonth] is 1..31. */
    data class MonthlyOnDay(val dayOfMonth: Int) : RecurrenceRule

    /** [weekOfMonth] is 1..4, or [LAST] for "last <weekday> of the month". */
    data class MonthlyOnWeekday(val weekOfMonth: Int, val dayOfWeek: DayOfWeek) : RecurrenceRule {
        companion object { const val LAST = -1 }
    }

    data class Yearly(val month: Month, val dayOfMonth: Int) : RecurrenceRule

    /** No reminders; doses are only logged by hand. */
    data object AsNeeded : RecurrenceRule
}
