package com.tunmed.pillswaterreminder.domain.reminder

import com.tunmed.pillswaterreminder.domain.model.MedicationStatus
import com.tunmed.pillswaterreminder.domain.model.RecurrenceRule
import com.tunmed.pillswaterreminder.domain.model.ReminderOccurrence
import com.tunmed.pillswaterreminder.domain.model.ScheduledMedication
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.daysUntil
import kotlinx.datetime.minus
import kotlinx.datetime.plus

class ScheduleEngine {

    fun isDueOn(rule: RecurrenceRule, date: LocalDate): Boolean = when (rule) {
        RecurrenceRule.Daily -> true

        is RecurrenceRule.EveryNDays -> {
            val days = rule.anchor.daysUntil(date)
            days >= 0 && days % rule.interval.coerceAtLeast(1) == 0
        }

        is RecurrenceRule.Weekly -> date.dayOfWeek in rule.days

        is RecurrenceRule.MonthlyOnDay -> date.day == minOf(rule.dayOfMonth, lengthOfMonth(date))

        is RecurrenceRule.MonthlyOnWeekday -> date.dayOfWeek == rule.dayOfWeek && if (rule.weekOfMonth == RecurrenceRule.MonthlyOnWeekday.LAST) {
            date.day + 7 > lengthOfMonth(date)
        } else {
            (date.day - 1) / 7 + 1 == rule.weekOfMonth
        }

        is RecurrenceRule.Yearly -> date.month == rule.month && date.day == minOf(rule.dayOfMonth, lengthOfMonth(date))

        RecurrenceRule.AsNeeded -> false
    }

    fun occurrencesOn(date: LocalDate, medications: List<ScheduledMedication>): List<ReminderOccurrence> {
        val result = mutableListOf<ReminderOccurrence>()
        for (item in medications) {
            val rule = item.schedule.rule
            val times = item.schedule.times.sorted()
            if (times.isEmpty()) continue

            if (item.isActiveOn(date) && isDueOn(rule, date)) {
                for (time in times) {
                    result += ReminderOccurrence.Dose(
                        at = LocalDateTime(date, time),
                        medicationId = item.medication.id,
                        scheduleId = item.schedule.id,
                    )
                }
            }

            val headsUpDays = item.schedule.headsUpDays
            if (headsUpDays > 0 && rule.supportsHeadsUp()) {
                val dueDate = date.plus(headsUpDays, DateTimeUnit.DAY)
                if (item.isActiveOn(dueDate) && isDueOn(rule, dueDate)) {
                    val time = times.first()
                    result += ReminderOccurrence.HeadsUp(
                        at = LocalDateTime(date, time),
                        medicationId = item.medication.id,
                        scheduleId = item.schedule.id,
                        dueAt = LocalDateTime(dueDate, time),
                    )
                }
            }
        }
        return result.sortedBy { it.at }
    }

    fun occurrencesBetween(
        fromExclusive: LocalDateTime,
        toInclusive: LocalDateTime,
        medications: List<ScheduledMedication>,
    ): List<ReminderOccurrence> {
        if (toInclusive <= fromExclusive || medications.isEmpty()) return emptyList()
        val result = mutableListOf<ReminderOccurrence>()
        var date = fromExclusive.date
        while (date <= toInclusive.date) {
            occurrencesOn(date, medications).filterTo(result) { it.at > fromExclusive && it.at <= toInclusive }
            date = date.plus(1, DateTimeUnit.DAY)
        }
        return result
    }

    fun nextAfter(
        from: LocalDateTime,
        medications: List<ScheduledMedication>,
        maxDays: Int = MAX_LOOKAHEAD_DAYS,
    ): ReminderOccurrence? {
        if (medications.isEmpty()) return null
        var date = from.date
        repeat(maxDays + 1) {
            occurrencesOn(date, medications).firstOrNull { it.at > from }?.let { return it }
            date = date.plus(1, DateTimeUnit.DAY)
        }
        return null
    }

    private fun ScheduledMedication.isActiveOn(date: LocalDate): Boolean {
        if (medication.status != MedicationStatus.ACTIVE) return false
        if (date < medication.startDate) return false
        val end = medication.endDate
        if (end != null && date > end) return false
        if (date < schedule.validFrom) return false
        val validTo = schedule.validTo
        if (validTo != null && date >= validTo) return false
        return true
    }

    private fun RecurrenceRule.supportsHeadsUp(): Boolean =
        this is RecurrenceRule.MonthlyOnDay || this is RecurrenceRule.MonthlyOnWeekday || this is RecurrenceRule.Yearly

    private fun lengthOfMonth(date: LocalDate): Int =
        LocalDate(date.year, date.month, 1).plus(1, DateTimeUnit.MONTH).minus(1, DateTimeUnit.DAY).day

    companion object {
        const val MAX_LOOKAHEAD_DAYS = 400
    }
}
