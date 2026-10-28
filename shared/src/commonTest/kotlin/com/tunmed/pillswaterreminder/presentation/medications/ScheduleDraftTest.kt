package com.tunmed.pillswaterreminder.presentation.medications

import com.tunmed.pillswaterreminder.domain.model.RecurrenceRule
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.Month

class ScheduleDraftTest {

    private val today = LocalDate(2026, 10, 26)

    @Test
    fun defaults_follow_today() {
        val draft = ScheduleDraft.startingOn(today)
        assertEquals(RecurrenceRule.Daily, draft.toRule())
        assertEquals(setOf(DayOfWeek.MONDAY), draft.weekdays)
        assertEquals(26, draft.dayOfMonth)
        assertEquals(4, draft.weekOfMonth)
        assertEquals(Month.OCTOBER, draft.month)
        assertEquals(today, draft.anchor)
    }

    @Test
    fun every_kind_builds_its_rule() {
        val draft = ScheduleDraft.startingOn(today)
        assertEquals(RecurrenceRule.EveryNDays(3, today), draft.copy(kind = RepeatKind.EVERY_N_DAYS, everyNDays = 3).toRule())
        assertEquals(
            RecurrenceRule.Weekly(setOf(DayOfWeek.MONDAY, DayOfWeek.FRIDAY)),
            draft.copy(kind = RepeatKind.WEEKLY, weekdays = setOf(DayOfWeek.MONDAY, DayOfWeek.FRIDAY)).toRule(),
        )
        assertEquals(RecurrenceRule.MonthlyOnDay(26), draft.copy(kind = RepeatKind.MONTHLY).toRule())
        assertEquals(
            RecurrenceRule.MonthlyOnWeekday(RecurrenceRule.MonthlyOnWeekday.LAST, DayOfWeek.FRIDAY),
            draft.copy(
                kind = RepeatKind.MONTHLY,
                monthlyByWeekday = true,
                weekOfMonth = RecurrenceRule.MonthlyOnWeekday.LAST,
                weekday = DayOfWeek.FRIDAY,
            ).toRule(),
        )
        assertEquals(RecurrenceRule.Yearly(Month.OCTOBER, 26), draft.copy(kind = RepeatKind.YEARLY).toRule())
        assertEquals(RecurrenceRule.AsNeeded, draft.copy(kind = RepeatKind.AS_NEEDED).toRule())
    }

    @Test
    fun weekly_without_a_day_is_not_ready() {
        assertNull(ScheduleDraft.startingOn(today).copy(kind = RepeatKind.WEEKLY, weekdays = emptySet()).toRule())
    }

    @Test
    fun yearly_day_is_clamped_to_the_month() {
        val draft = ScheduleDraft.startingOn(today).copy(kind = RepeatKind.YEARLY, month = Month.FEBRUARY, dayOfMonth = 31)
        assertEquals(RecurrenceRule.Yearly(Month.FEBRUARY, 29), draft.toRule())
    }

    @Test
    fun heads_up_is_saved_only_for_monthly_and_yearly() {
        val draft = ScheduleDraft.startingOn(today).copy(headsUpDays = 3)
        assertEquals(0, draft.copy(kind = RepeatKind.WEEKLY).headsUpToSave())
        assertEquals(3, draft.copy(kind = RepeatKind.MONTHLY).headsUpToSave())
        assertEquals(3, draft.copy(kind = RepeatKind.YEARLY).headsUpToSave())
    }

    @Test
    fun a_saved_rule_reopens_unchanged() {
        val rules = listOf(
            RecurrenceRule.Daily,
            RecurrenceRule.EveryNDays(5, LocalDate(2026, 9, 1)),
            RecurrenceRule.Weekly(setOf(DayOfWeek.TUESDAY, DayOfWeek.SUNDAY)),
            RecurrenceRule.MonthlyOnDay(31),
            RecurrenceRule.MonthlyOnWeekday(2, DayOfWeek.TUESDAY),
            RecurrenceRule.Yearly(Month.FEBRUARY, 29),
            RecurrenceRule.AsNeeded,
        )
        for (rule in rules) {
            assertEquals(rule, ScheduleDraft.of(rule, headsUpDays = 2, today = today).toRule(), "$rule")
        }
        assertEquals(2, ScheduleDraft.of(RecurrenceRule.MonthlyOnDay(18), 2, today).headsUpToSave())
    }
}
