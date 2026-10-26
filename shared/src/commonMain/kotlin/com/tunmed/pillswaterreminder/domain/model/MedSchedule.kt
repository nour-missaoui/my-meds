package com.tunmed.pillswaterreminder.domain.model

import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime

/**
 * A versioned schedule for one medication. Editing a medication closes the current
 * version ([validTo] = today) and opens a new one, so past DoseLogs keep pointing
 * at the version they were taken under.
 */
data class MedSchedule(
    val id: Long = 0,
    val medicationId: Long,
    val rule: RecurrenceRule,
    val times: List<LocalTime>,
    /** Monthly / yearly heads-up, e.g. 1 = remind the day before. 0 = no heads-up. */
    val headsUpDays: Int = 0,
    val validFrom: LocalDate,
    val validTo: LocalDate? = null,
)
