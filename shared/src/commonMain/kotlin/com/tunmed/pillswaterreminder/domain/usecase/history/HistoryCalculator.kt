package com.tunmed.pillswaterreminder.domain.usecase.history

import com.tunmed.pillswaterreminder.domain.model.DayStatus
import com.tunmed.pillswaterreminder.domain.model.DoseStatus
import com.tunmed.pillswaterreminder.domain.model.HistoryEntry
import com.tunmed.pillswaterreminder.domain.model.MedicationAdherence

object HistoryCalculator {

    fun dayStatus(entries: List<HistoryEntry>): DayStatus {
        val scheduled = entries.filterNot { it.asNeeded }.map { it.log.status }
        return when {
            scheduled.isEmpty() -> DayStatus.NONE
            DoseStatus.MISSED in scheduled -> DayStatus.MISSED
            scheduled.any { it == DoseStatus.PENDING || it == DoseStatus.SNOOZED } -> DayStatus.IN_PROGRESS
            scheduled.all { it == DoseStatus.TAKEN } -> DayStatus.ALL_TAKEN
            else -> DayStatus.PARTIAL
        }
    }

    fun adherence(entries: List<HistoryEntry>): List<MedicationAdherence> =
        entries.filterNot { it.asNeeded }
            .groupBy { it.medication.id }
            .map { (_, forMedication) ->
                val statuses = forMedication.map { it.log.status }
                MedicationAdherence(
                    medication = forMedication.first().medication,
                    taken = statuses.count { it == DoseStatus.TAKEN },
                    skipped = statuses.count { it == DoseStatus.SKIPPED },
                    missed = statuses.count { it == DoseStatus.MISSED },
                )
            }
            .filter { it.answered > 0 }
            .sortedBy { it.medication.name.lowercase() }

    fun overallPercent(adherence: List<MedicationAdherence>): Int? {
        val answered = adherence.sumOf { it.answered }
        if (answered == 0) return null
        return adherence.sumOf { it.taken } * 100 / answered
    }
}
