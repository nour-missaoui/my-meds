package com.tunmed.pillswaterreminder.domain.usecase.today

import com.tunmed.pillswaterreminder.core.time.TimeProvider
import com.tunmed.pillswaterreminder.domain.model.AsNeededToday
import com.tunmed.pillswaterreminder.domain.model.DoseStatus
import com.tunmed.pillswaterreminder.domain.model.RecurrenceRule
import com.tunmed.pillswaterreminder.domain.repository.DoseLogRepository
import com.tunmed.pillswaterreminder.domain.repository.MedicationRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.plus

class ObserveAsNeededTodayUseCase(
    private val medicationRepository: MedicationRepository,
    private val doseLogRepository: DoseLogRepository,
    private val timeProvider: TimeProvider,
) {
    operator fun invoke(date: LocalDate): Flow<List<AsNeededToday>> {
        val zone = timeProvider.timeZone()
        val from = date.atStartOfDayIn(zone)
        val to = date.plus(1, DateTimeUnit.DAY).atStartOfDayIn(zone)
        return combine(
            medicationRepository.observeScheduledMedications(),
            doseLogRepository.observeBetween(from, to),
        ) { scheduled, logs ->
            scheduled.filter { it.schedule.rule == RecurrenceRule.AsNeeded }.map { item ->
                val taken = logs.filter { it.medicationId == item.medication.id && it.status == DoseStatus.TAKEN }
                AsNeededToday(
                    medication = item.medication,
                    takenCount = taken.size,
                    lastTakenAt = taken.maxOfOrNull { it.actedAt ?: it.scheduledAt },
                )
            }
        }
    }
}
