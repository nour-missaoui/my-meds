package com.tunmed.pillswaterreminder.domain.usecase.history

import com.tunmed.pillswaterreminder.core.time.TimeProvider
import com.tunmed.pillswaterreminder.domain.model.HistoryEntry
import com.tunmed.pillswaterreminder.domain.model.HistoryMonth
import com.tunmed.pillswaterreminder.domain.model.RecurrenceRule
import com.tunmed.pillswaterreminder.domain.repository.DoseLogRepository
import com.tunmed.pillswaterreminder.domain.repository.MedicationRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime

class ObserveHistoryUseCase(
    private val medicationRepository: MedicationRepository,
    private val doseLogRepository: DoseLogRepository,
    private val timeProvider: TimeProvider,
) {
    operator fun invoke(firstDayOfMonth: LocalDate): Flow<HistoryMonth> {
        val zone = timeProvider.timeZone()
        val from = firstDayOfMonth.atStartOfDayIn(zone)
        val to = firstDayOfMonth.plus(1, DateTimeUnit.MONTH).atStartOfDayIn(zone)
        return combine(
            medicationRepository.observeMedications(),
            medicationRepository.observeCurrentSchedules(),
            doseLogRepository.observeBetween(from, to),
        ) { medications, schedules, logs ->
            val byId = medications.associateBy { it.id }
            val asNeededIds = schedules.filter { it.rule == RecurrenceRule.AsNeeded }.map { it.medicationId }.toSet()
            val entries = logs.mapNotNull { log ->
                val medication = byId[log.medicationId] ?: return@mapNotNull null
                val local = log.scheduledAt.toLocalDateTime(zone)
                local.date to HistoryEntry(
                    log = log,
                    medication = medication,
                    time = local.time,
                    asNeeded = log.medicationId in asNeededIds,
                )
            }
            val entriesByDay = entries.groupBy({ it.first }, { it.second })
                .mapValues { (_, list) -> list.sortedBy { it.log.scheduledAt } }
            val adherence = HistoryCalculator.adherence(entries.map { it.second })
            HistoryMonth(
                firstDay = firstDayOfMonth,
                entriesByDay = entriesByDay,
                dayStatus = entriesByDay.mapValues { (_, list) -> HistoryCalculator.dayStatus(list) },
                adherence = adherence,
                overallPercent = HistoryCalculator.overallPercent(adherence),
            )
        }
    }
}
