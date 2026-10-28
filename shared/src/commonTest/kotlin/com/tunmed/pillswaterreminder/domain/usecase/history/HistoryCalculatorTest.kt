package com.tunmed.pillswaterreminder.domain.usecase.history

import com.tunmed.pillswaterreminder.domain.model.DayStatus
import com.tunmed.pillswaterreminder.domain.model.DoseLog
import com.tunmed.pillswaterreminder.domain.model.DoseStatus
import com.tunmed.pillswaterreminder.domain.model.HistoryEntry
import com.tunmed.pillswaterreminder.domain.model.Medication
import com.tunmed.pillswaterreminder.domain.model.MedicationForm
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.time.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime

class HistoryCalculatorTest {

    private val metformin = Medication(id = 1, name = "Metformin", dose = "500 mg", form = MedicationForm.TABLET, startDate = LocalDate(2026, 10, 1))
    private val aspirin = Medication(id = 2, name = "Aspirin", dose = "100 mg", form = MedicationForm.TABLET, startDate = LocalDate(2026, 10, 1))
    private val painkiller = Medication(id = 3, name = "Ibuprofen", dose = "400 mg", form = MedicationForm.TABLET, startDate = LocalDate(2026, 10, 1))

    private var nextId = 1L

    private fun entry(medication: Medication, status: DoseStatus, asNeeded: Boolean = false) = HistoryEntry(
        log = DoseLog(
            id = nextId++,
            medicationId = medication.id,
            scheduleId = medication.id * 10,
            scheduledAt = Instant.fromEpochMilliseconds(nextId * 60_000),
            status = status,
        ),
        medication = medication,
        time = LocalTime(8, 0),
        asNeeded = asNeeded,
    )

    @Test
    fun day_status_follows_the_worst_scheduled_dose() {
        assertEquals(DayStatus.NONE, HistoryCalculator.dayStatus(emptyList()))
        assertEquals(DayStatus.ALL_TAKEN, HistoryCalculator.dayStatus(listOf(entry(metformin, DoseStatus.TAKEN), entry(aspirin, DoseStatus.TAKEN))))
        assertEquals(DayStatus.PARTIAL, HistoryCalculator.dayStatus(listOf(entry(metformin, DoseStatus.TAKEN), entry(aspirin, DoseStatus.SKIPPED))))
        assertEquals(DayStatus.MISSED, HistoryCalculator.dayStatus(listOf(entry(metformin, DoseStatus.MISSED), entry(aspirin, DoseStatus.PENDING))))
        assertEquals(DayStatus.IN_PROGRESS, HistoryCalculator.dayStatus(listOf(entry(metformin, DoseStatus.TAKEN), entry(aspirin, DoseStatus.SNOOZED))))
    }

    @Test
    fun as_needed_doses_do_not_colour_the_day() {
        assertEquals(DayStatus.NONE, HistoryCalculator.dayStatus(listOf(entry(painkiller, DoseStatus.TAKEN, asNeeded = true))))
    }

    @Test
    fun adherence_counts_answered_scheduled_doses_only() {
        val entries = listOf(
            entry(metformin, DoseStatus.TAKEN),
            entry(metformin, DoseStatus.TAKEN),
            entry(metformin, DoseStatus.TAKEN),
            entry(metformin, DoseStatus.MISSED),
            entry(metformin, DoseStatus.PENDING),
            entry(aspirin, DoseStatus.SKIPPED),
            entry(painkiller, DoseStatus.TAKEN, asNeeded = true),
        )
        val adherence = HistoryCalculator.adherence(entries)
        assertEquals(listOf("Aspirin", "Metformin"), adherence.map { it.medication.name })
        assertEquals(75, adherence.first { it.medication.id == 1L }.percent)
        assertEquals(0, adherence.first { it.medication.id == 2L }.percent)
        assertEquals(60, HistoryCalculator.overallPercent(adherence))
    }

    @Test
    fun no_answered_doses_means_no_percentage() {
        assertNull(HistoryCalculator.overallPercent(HistoryCalculator.adherence(listOf(entry(metformin, DoseStatus.PENDING)))))
    }
}
