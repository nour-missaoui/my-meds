package com.tunmed.pillswaterreminder.domain.usecase.today

import com.tunmed.pillswaterreminder.core.time.TimeProvider
import com.tunmed.pillswaterreminder.domain.model.ReminderOccurrence
import com.tunmed.pillswaterreminder.domain.model.TodayDose
import com.tunmed.pillswaterreminder.domain.reminder.ScheduleEngine
import com.tunmed.pillswaterreminder.domain.repository.DoseLogRepository
import com.tunmed.pillswaterreminder.domain.repository.MedicationRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.plus
import kotlinx.datetime.toInstant

class ObserveTodayDosesUseCase(
    private val medicationRepository: MedicationRepository,
    private val doseLogRepository: DoseLogRepository,
    private val engine: ScheduleEngine,
    private val timeProvider: TimeProvider,
) {
    operator fun invoke(date: LocalDate): Flow<List<TodayDose>> {
        val zone = timeProvider.timeZone()
        val from = date.atStartOfDayIn(zone)
        val to = date.plus(1, DateTimeUnit.DAY).atStartOfDayIn(zone)
        return combine(
            medicationRepository.observeScheduledMedications(),
            doseLogRepository.observeBetween(from, to),
        ) { scheduled, logs ->
            val byMedication = scheduled.associateBy { it.medication.id }
            val logBySlot = logs.associateBy { it.medicationId to it.scheduledAt }
            engine.occurrencesOn(date, scheduled)
                .filterIsInstance<ReminderOccurrence.Dose>()
                .mapNotNull { dose ->
                    val item = byMedication[dose.medicationId] ?: return@mapNotNull null
                    val medication = item.medication
                    val at = dose.at.toInstant(zone)
                    val log = logBySlot[dose.medicationId to at]
                    TodayDose(
                        time = dose.at.time,
                        medication = medication,
                        doseText = item.schedule.doseTextAt(dose.at.time, medication.dose),
                        scheduleId = dose.scheduleId,
                        scheduledAt = at,
                        status = log?.status,
                        actedAt = log?.actedAt,
                    )
                }
        }
    }
}
