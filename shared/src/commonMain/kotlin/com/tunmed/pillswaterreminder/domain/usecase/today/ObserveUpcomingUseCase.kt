package com.tunmed.pillswaterreminder.domain.usecase.today

import com.tunmed.pillswaterreminder.domain.model.RecurrenceRule
import com.tunmed.pillswaterreminder.domain.model.ReminderOccurrence
import com.tunmed.pillswaterreminder.domain.reminder.ScheduleEngine
import com.tunmed.pillswaterreminder.domain.reminder.UpcomingDose
import com.tunmed.pillswaterreminder.domain.repository.MedicationRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.plus

class ObserveUpcomingUseCase(
    private val medicationRepository: MedicationRepository,
    private val engine: ScheduleEngine,
) {
    operator fun invoke(today: LocalDate): Flow<List<UpcomingDose>> =
        medicationRepository.observeScheduledMedications().map { scheduled ->
            val longRange = scheduled.filter { it.schedule.rule.isMonthlyOrYearly() }
            (1..DAYS_AHEAD).flatMap { offset ->
                val date = today.plus(offset, DateTimeUnit.DAY)
                engine.occurrencesOn(date, longRange)
                    .filterIsInstance<ReminderOccurrence.Dose>()
                    .mapNotNull { dose ->
                        val medication = longRange.firstOrNull { it.medication.id == dose.medicationId }?.medication
                        medication?.let { UpcomingDose(it, dose.at, offset) }
                    }
            }
        }

    private fun RecurrenceRule.isMonthlyOrYearly(): Boolean =
        this is RecurrenceRule.MonthlyOnDay || this is RecurrenceRule.MonthlyOnWeekday || this is RecurrenceRule.Yearly

    private companion object {
        const val DAYS_AHEAD = 7
    }
}
