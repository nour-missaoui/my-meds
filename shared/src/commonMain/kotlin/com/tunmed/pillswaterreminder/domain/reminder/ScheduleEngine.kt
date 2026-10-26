package com.tunmed.pillswaterreminder.domain.reminder

import com.tunmed.pillswaterreminder.domain.model.RecurrenceRule
import com.tunmed.pillswaterreminder.domain.model.ReminderOccurrence
import com.tunmed.pillswaterreminder.domain.model.ScheduledMedication
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime

/**
 * Pure scheduling logic: rules in, occurrences out. No I/O, no clock, no platform code,
 * so every case can be unit-tested on the JVM (see ScheduleEngineTest, spec S-1 to S-7).
 */
class ScheduleEngine {

    /** Whether a medication following [rule] is due on [date] (S-1 to S-6). */
    fun isDueOn(rule: RecurrenceRule, date: LocalDate): Boolean =
        TODO("Phase 1: Daily, EveryNDays, Weekly, MonthlyOnDay (clamp), MonthlyOnWeekday, Yearly (29 Feb), AsNeeded")

    /**
     * All doses and heads-ups on [date], sorted by time. A dose is only due between the
     * medication's start and end dates and inside the schedule's validFrom / validTo.
     */
    fun occurrencesOn(date: LocalDate, medications: List<ScheduledMedication>): List<ReminderOccurrence> =
        TODO("Phase 1: isDueOn() per medication x schedule.times, plus heads-ups (S-7)")

    /** The first occurrence strictly after [from]; used to arm the next alarm. */
    fun nextAfter(from: LocalDateTime, medications: List<ScheduledMedication>): ReminderOccurrence? =
        TODO("Phase 1: scan forward day by day, at most ~400 days to cover yearly rules")
}
