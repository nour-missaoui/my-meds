package com.tunmed.pillswaterreminder.domain.model

import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.Month

sealed interface RecurrenceRule {
    data object Daily : RecurrenceRule

    data class EveryNDays(val interval: Int, val anchor: LocalDate) : RecurrenceRule

    data class Weekly(val days: Set<DayOfWeek>) : RecurrenceRule

    data class MonthlyOnDay(val dayOfMonth: Int) : RecurrenceRule

    data class MonthlyOnWeekday(val weekOfMonth: Int, val dayOfWeek: DayOfWeek) : RecurrenceRule {
        companion object { const val LAST = -1 }
    }

    data class Yearly(val month: Month, val dayOfMonth: Int) : RecurrenceRule

    data object AsNeeded : RecurrenceRule
}
