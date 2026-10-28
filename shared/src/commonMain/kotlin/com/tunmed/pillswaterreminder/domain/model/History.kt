package com.tunmed.pillswaterreminder.domain.model

import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime

enum class DayStatus { NONE, ALL_TAKEN, PARTIAL, MISSED, IN_PROGRESS }

data class HistoryEntry(
    val log: DoseLog,
    val medication: Medication,
    val time: LocalTime,
    val asNeeded: Boolean,
)

data class MedicationAdherence(
    val medication: Medication,
    val taken: Int,
    val skipped: Int,
    val missed: Int,
) {
    val answered: Int get() = taken + skipped + missed
    val percent: Int? get() = if (answered == 0) null else taken * 100 / answered
}

data class HistoryMonth(
    val firstDay: LocalDate,
    val entriesByDay: Map<LocalDate, List<HistoryEntry>>,
    val dayStatus: Map<LocalDate, DayStatus>,
    val adherence: List<MedicationAdherence>,
    val overallPercent: Int?,
)
