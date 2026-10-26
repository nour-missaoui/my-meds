package com.tunmed.pillswaterreminder.domain.reminder

import com.tunmed.pillswaterreminder.domain.model.MedSchedule
import com.tunmed.pillswaterreminder.domain.model.Medication
import com.tunmed.pillswaterreminder.domain.model.MedicationForm
import com.tunmed.pillswaterreminder.domain.model.MedicationStatus
import com.tunmed.pillswaterreminder.domain.model.RecurrenceRule
import com.tunmed.pillswaterreminder.domain.model.ReminderOccurrence
import com.tunmed.pillswaterreminder.domain.model.ScheduledMedication
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.Month

class ScheduleEngineTest {

    private val engine = ScheduleEngine()

    @Test
    fun s1_daily_is_due_every_day() {
        assertTrue(engine.isDueOn(RecurrenceRule.Daily, LocalDate(2026, 10, 26)))
        assertTrue(engine.isDueOn(RecurrenceRule.Daily, LocalDate(2027, 2, 28)))
    }

    @Test
    fun s2_every_n_days_counts_from_anchor() {
        val rule = RecurrenceRule.EveryNDays(interval = 3, anchor = LocalDate(2026, 10, 15))
        assertTrue(engine.isDueOn(rule, LocalDate(2026, 10, 15)))
        assertTrue(engine.isDueOn(rule, LocalDate(2026, 10, 18)))
        assertFalse(engine.isDueOn(rule, LocalDate(2026, 10, 17)))
        assertFalse(engine.isDueOn(rule, LocalDate(2026, 10, 12)), "never due before the anchor")
    }

    @Test
    fun s3_weekly_on_selected_weekdays() {
        val rule = RecurrenceRule.Weekly(setOf(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY))
        assertTrue(engine.isDueOn(rule, LocalDate(2026, 10, 26)))
        assertFalse(engine.isDueOn(rule, LocalDate(2026, 10, 27)))
        assertTrue(engine.isDueOn(rule, LocalDate(2026, 10, 30)))
    }

    @Test
    fun s4_monthly_on_31st_falls_on_last_day_of_short_months() {
        val rule = RecurrenceRule.MonthlyOnDay(31)
        assertTrue(engine.isDueOn(rule, LocalDate(2026, 10, 31)))
        assertTrue(engine.isDueOn(rule, LocalDate(2026, 11, 30)))
        assertTrue(engine.isDueOn(rule, LocalDate(2027, 2, 28)))
        assertFalse(engine.isDueOn(rule, LocalDate(2026, 11, 29)))
    }

    @Test
    fun s4_monthly_on_18th() {
        assertTrue(engine.isDueOn(RecurrenceRule.MonthlyOnDay(18), LocalDate(2026, 11, 18)))
        assertFalse(engine.isDueOn(RecurrenceRule.MonthlyOnDay(18), LocalDate(2026, 11, 17)))
    }

    @Test
    fun s5_last_friday_of_the_month() {
        val rule = RecurrenceRule.MonthlyOnWeekday(RecurrenceRule.MonthlyOnWeekday.LAST, DayOfWeek.FRIDAY)
        assertTrue(engine.isDueOn(rule, LocalDate(2026, 10, 30)))
        assertFalse(engine.isDueOn(rule, LocalDate(2026, 10, 23)))
    }

    @Test
    fun s5_first_monday_of_the_month() {
        val rule = RecurrenceRule.MonthlyOnWeekday(1, DayOfWeek.MONDAY)
        assertTrue(engine.isDueOn(rule, LocalDate(2026, 11, 2)))
        assertFalse(engine.isDueOn(rule, LocalDate(2026, 11, 9)))
        assertFalse(engine.isDueOn(rule, LocalDate(2026, 11, 3)), "Tuesday")
    }

    @Test
    fun s6_yearly_on_29_february_falls_on_28_february_in_non_leap_years() {
        val rule = RecurrenceRule.Yearly(Month.FEBRUARY, 29)
        assertTrue(engine.isDueOn(rule, LocalDate(2027, 2, 28)))
        assertTrue(engine.isDueOn(rule, LocalDate(2028, 2, 29)))
        assertFalse(engine.isDueOn(rule, LocalDate(2028, 2, 28)))
    }

    @Test
    fun m4_as_needed_is_never_due() {
        assertFalse(engine.isDueOn(RecurrenceRule.AsNeeded, LocalDate(2026, 10, 26)))
    }

    @Test
    fun daily_doses_are_sorted_by_time() {
        val med = scheduled(RecurrenceRule.Daily, listOf(LocalTime(20, 0), LocalTime(8, 0)))
        val doses = engine.occurrencesOn(LocalDate(2026, 10, 26), listOf(med))
        assertEquals(listOf(LocalTime(8, 0), LocalTime(20, 0)), doses.map { it.at.time })
        assertTrue(doses.all { it is ReminderOccurrence.Dose })
    }

    @Test
    fun no_doses_outside_start_and_end_dates_or_when_paused() {
        val start = LocalDate(2026, 10, 20)
        val end = LocalDate(2026, 10, 25)
        val med = scheduled(RecurrenceRule.Daily, listOf(LocalTime(8, 0)), start = start, end = end)
        assertTrue(engine.occurrencesOn(LocalDate(2026, 10, 19), listOf(med)).isEmpty())
        assertEquals(1, engine.occurrencesOn(LocalDate(2026, 10, 25), listOf(med)).size)
        assertTrue(engine.occurrencesOn(LocalDate(2026, 10, 26), listOf(med)).isEmpty())

        val paused = scheduled(RecurrenceRule.Daily, listOf(LocalTime(8, 0)), status = MedicationStatus.PAUSED)
        assertTrue(engine.occurrencesOn(LocalDate(2026, 10, 26), listOf(paused)).isEmpty())
    }

    @Test
    fun closed_schedule_version_stops_on_its_validTo_date() {
        val med = scheduled(RecurrenceRule.Daily, listOf(LocalTime(8, 0)))
        val closed = med.copy(schedule = med.schedule.copy(validTo = LocalDate(2026, 10, 26)))
        assertEquals(1, engine.occurrencesOn(LocalDate(2026, 10, 25), listOf(closed)).size)
        assertTrue(engine.occurrencesOn(LocalDate(2026, 10, 26), listOf(closed)).isEmpty())
    }

    @Test
    fun s7_heads_up_the_day_before_a_monthly_dose() {
        val med = scheduled(RecurrenceRule.MonthlyOnDay(18), listOf(LocalTime(10, 0)), headsUpDays = 1)
        val dayBefore = engine.occurrencesOn(LocalDate(2026, 11, 17), listOf(med))
        val headsUp = dayBefore.single() as ReminderOccurrence.HeadsUp
        assertEquals(LocalDateTime(LocalDate(2026, 11, 17), LocalTime(10, 0)), headsUp.at)
        assertEquals(LocalDateTime(LocalDate(2026, 11, 18), LocalTime(10, 0)), headsUp.dueAt)
        assertTrue(engine.occurrencesOn(LocalDate(2026, 11, 18), listOf(med)).single() is ReminderOccurrence.Dose)
    }

    @Test
    fun s7_no_heads_up_for_daily_schedules() {
        val med = scheduled(RecurrenceRule.Daily, listOf(LocalTime(8, 0)), headsUpDays = 1)
        assertTrue(engine.occurrencesOn(LocalDate(2026, 10, 26), listOf(med)).all { it is ReminderOccurrence.Dose })
    }

    @Test
    fun between_spans_midnight_and_excludes_the_start() {
        val med = scheduled(RecurrenceRule.Daily, listOf(LocalTime(8, 0), LocalTime(22, 0)))
        val from = LocalDateTime(LocalDate(2026, 10, 26), LocalTime(8, 0))
        val to = LocalDateTime(LocalDate(2026, 10, 27), LocalTime(9, 0))
        val found = engine.occurrencesBetween(from, to, listOf(med)).map { it.at }
        assertEquals(
            listOf(
                LocalDateTime(LocalDate(2026, 10, 26), LocalTime(22, 0)),
                LocalDateTime(LocalDate(2026, 10, 27), LocalTime(8, 0)),
            ),
            found,
        )
    }

    @Test
    fun next_daily_dose_after_the_morning_one_is_tomorrow() {
        val med = scheduled(RecurrenceRule.Daily, listOf(LocalTime(8, 0)))
        val next = engine.nextAfter(LocalDateTime(LocalDate(2026, 10, 26), LocalTime(9, 0)), listOf(med))
        assertEquals(LocalDateTime(LocalDate(2026, 10, 27), LocalTime(8, 0)), next?.at)
    }

    @Test
    fun next_yearly_dose_is_found_almost_a_year_ahead() {
        val med = scheduled(RecurrenceRule.Yearly(Month.OCTOBER, 20), listOf(LocalTime(9, 0)))
        val next = engine.nextAfter(LocalDateTime(LocalDate(2026, 10, 26), LocalTime(9, 0)), listOf(med))
        assertEquals(LocalDateTime(LocalDate(2027, 10, 20), LocalTime(9, 0)), next?.at)
    }

    @Test
    fun as_needed_only_has_no_next_dose() {
        val med = scheduled(RecurrenceRule.AsNeeded, listOf(LocalTime(8, 0)))
        assertNull(engine.nextAfter(LocalDateTime(LocalDate(2026, 10, 26), LocalTime(9, 0)), listOf(med)))
    }

    private fun scheduled(
        rule: RecurrenceRule,
        times: List<LocalTime>,
        start: LocalDate = LocalDate(2026, 1, 1),
        end: LocalDate? = null,
        status: MedicationStatus = MedicationStatus.ACTIVE,
        headsUpDays: Int = 0,
    ) = ScheduledMedication(
        medication = Medication(
            id = 1,
            name = "Test",
            dose = "1 tablet",
            form = MedicationForm.TABLET,
            startDate = start,
            endDate = end,
            status = status,
        ),
        schedule = MedSchedule(
            id = 10,
            medicationId = 1,
            rule = rule,
            times = times,
            headsUpDays = headsUpDays,
            validFrom = start,
        ),
    )
}
