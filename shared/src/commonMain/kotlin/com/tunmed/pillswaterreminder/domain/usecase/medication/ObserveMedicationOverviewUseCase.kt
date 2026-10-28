package com.tunmed.pillswaterreminder.domain.usecase.medication

import com.tunmed.pillswaterreminder.core.time.TimeProvider
import com.tunmed.pillswaterreminder.domain.model.MedSchedule
import com.tunmed.pillswaterreminder.domain.model.Medication
import com.tunmed.pillswaterreminder.domain.model.MedicationOverview
import com.tunmed.pillswaterreminder.domain.model.MedicationStatus
import com.tunmed.pillswaterreminder.domain.model.ReminderOccurrence
import com.tunmed.pillswaterreminder.domain.model.ScheduledMedication
import com.tunmed.pillswaterreminder.domain.reminder.ScheduleEngine
import com.tunmed.pillswaterreminder.domain.repository.MedicationRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.toLocalDateTime

class ObserveMedicationOverviewUseCase(
    private val repository: MedicationRepository,
    private val engine: ScheduleEngine,
    private val timeProvider: TimeProvider,
) {
    operator fun invoke(): Flow<List<MedicationOverview>> =
        combine(repository.observeMedications(), repository.observeCurrentSchedules()) { medications, schedules ->
            val now = timeProvider.now().toLocalDateTime(timeProvider.timeZone())
            val scheduleByMedication = schedules.associateBy { it.medicationId }
            medications.map { medication ->
                val schedule = scheduleByMedication[medication.id]
                MedicationOverview(
                    medication = medication,
                    schedule = schedule,
                    nextDoseAt = nextDose(medication, schedule, now),
                )
            }
        }

    private fun nextDose(medication: Medication, schedule: MedSchedule?, from: LocalDateTime): LocalDateTime? {
        if (schedule == null || medication.status != MedicationStatus.ACTIVE) return null
        val item = listOf(ScheduledMedication(medication, schedule))
        var cursor = from
        repeat(MAX_SKIPS) {
            val next = engine.nextAfter(cursor, item) ?: return null
            if (next is ReminderOccurrence.Dose) return next.at
            cursor = next.at
        }
        return null
    }

    private companion object {
        const val MAX_SKIPS = 10
    }
}
