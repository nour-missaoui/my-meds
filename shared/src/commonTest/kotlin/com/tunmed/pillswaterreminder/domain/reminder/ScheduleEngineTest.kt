package com.tunmed.pillswaterreminder.domain.reminder

import com.tunmed.pillswaterreminder.domain.model.RecurrenceRule
import kotlin.test.Ignore
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.Month

/**
 * One test (or more) per schedule in the spec (S-1 to S-7).
 * Phase 1: implement ScheduleEngine.isDueOn and remove each @Ignore as the rule passes.
 */
class ScheduleEngineTest {

    private val engine = ScheduleEngine()

    @Ignore
    @Test
    fun s1_daily_is_due_every_day() {
        assertTrue(engine.isDueOn(RecurrenceRule.Daily, LocalDate(2026, 10, 26)))
        assertTrue(engine.isDueOn(RecurrenceRule.Daily, LocalDate(2027, 2, 28)))
    }

    @Ignore
    @Test
    fun s2_every_n_days_counts_from_anchor() {
        val rule = RecurrenceRule.EveryNDays(interval = 3, anchor = LocalDate(2026, 10, 15))
        assertTrue(engine.isDueOn(rule, LocalDate(2026, 10, 15)))
        assertTrue(engine.isDueOn(rule, LocalDate(2026, 10, 18)))
        assertFalse(engine.isDueOn(rule, LocalDate(2026, 10, 17)))
    }

    @Ignore
    @Test
    fun s3_weekly_on_selected_weekdays() {
        val rule = RecurrenceRule.Weekly(setOf(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY))
        assertTrue(engine.isDueOn(rule, LocalDate(2026, 10, 26))) // Monday
        assertFalse(engine.isDueOn(rule, LocalDate(2026, 10, 27))) // Tuesday
    }

    @Ignore
    @Test
    fun s4_monthly_on_31st_falls_on_last_day_of_short_months() {
        assertTrue(engine.isDueOn(RecurrenceRule.MonthlyOnDay(31), LocalDate(2026, 11, 30)))
        assertTrue(engine.isDueOn(RecurrenceRule.MonthlyOnDay(31), LocalDate(2027, 2, 28)))
        assertFalse(engine.isDueOn(RecurrenceRule.MonthlyOnDay(31), LocalDate(2026, 11, 29)))
    }

    @Ignore
    @Test
    fun s5_last_friday_of_the_month() {
        val rule = RecurrenceRule.MonthlyOnWeekday(RecurrenceRule.MonthlyOnWeekday.LAST, DayOfWeek.FRIDAY)
        assertTrue(engine.isDueOn(rule, LocalDate(2026, 10, 30)))
        assertFalse(engine.isDueOn(rule, LocalDate(2026, 10, 23)))
    }

    @Ignore
    @Test
    fun s5_first_monday_of_the_month() {
        val rule = RecurrenceRule.MonthlyOnWeekday(1, DayOfWeek.MONDAY)
        assertTrue(engine.isDueOn(rule, LocalDate(2026, 11, 2)))
        assertFalse(engine.isDueOn(rule, LocalDate(2026, 11, 9)))
    }

    @Ignore
    @Test
    fun s6_yearly_on_29_february_falls_on_28_february_in_non_leap_years() {
        val rule = RecurrenceRule.Yearly(Month.FEBRUARY, 29)
        assertTrue(engine.isDueOn(rule, LocalDate(2027, 2, 28)))
        assertTrue(engine.isDueOn(rule, LocalDate(2028, 2, 29)))
        assertFalse(engine.isDueOn(rule, LocalDate(2028, 2, 28)))
    }

    @Ignore
    @Test
    fun m4_as_needed_is_never_due() {
        assertFalse(engine.isDueOn(RecurrenceRule.AsNeeded, LocalDate(2026, 10, 26)))
    }
}
