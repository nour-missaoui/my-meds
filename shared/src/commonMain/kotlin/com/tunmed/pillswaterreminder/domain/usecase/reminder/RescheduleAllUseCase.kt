package com.tunmed.pillswaterreminder.domain.usecase.reminder

import com.tunmed.pillswaterreminder.core.time.TimeProvider
import com.tunmed.pillswaterreminder.domain.model.DoseLog
import com.tunmed.pillswaterreminder.domain.model.DoseStatus
import com.tunmed.pillswaterreminder.domain.model.ReminderOccurrence
import com.tunmed.pillswaterreminder.domain.reminder.DueDose
import com.tunmed.pillswaterreminder.domain.reminder.ReminderNotifier
import com.tunmed.pillswaterreminder.domain.reminder.ReminderScheduler
import com.tunmed.pillswaterreminder.domain.reminder.ScheduleEngine
import com.tunmed.pillswaterreminder.domain.reminder.ScheduledAlarm
import com.tunmed.pillswaterreminder.domain.reminder.UpcomingDose
import com.tunmed.pillswaterreminder.domain.repository.DoseLogRepository
import com.tunmed.pillswaterreminder.domain.repository.MedicationRepository
import com.tunmed.pillswaterreminder.domain.repository.ReminderStateRepository
import com.tunmed.pillswaterreminder.domain.repository.SettingsRepository
import kotlin.time.Duration
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Instant
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.datetime.daysUntil
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime

class RescheduleAllUseCase(
    private val engine: ScheduleEngine,
    private val medicationRepository: MedicationRepository,
    private val doseLogRepository: DoseLogRepository,
    private val settingsRepository: SettingsRepository,
    private val reminderStateRepository: ReminderStateRepository,
    private val scheduler: ReminderScheduler,
    private val notifier: ReminderNotifier,
    private val timeProvider: TimeProvider,
) {
    private val mutex = Mutex()

    suspend operator fun invoke() = mutex.withLock {
        val now = timeProvider.now()
        val zone = timeProvider.timeZone()
        val nowLocal = now.toLocalDateTime(zone)
        val settings = settingsRepository.observe().first()
        val missedAfter = settings.missedAfterMinutes.minutes
        val snooze = settings.snoozeMinutes.minutes
        val scheduled = medicationRepository.getScheduledMedications()
        val byMedication = scheduled.associateBy { it.medication.id }

        val lastRun = reminderStateRepository.getLastCycleAt()
        if (lastRun != null && lastRun < now) {
            val from = maxOf(lastRun, now - MAX_CATCH_UP).toLocalDateTime(zone)
            for (occurrence in engine.occurrencesBetween(from, nowLocal, scheduled)) {
                val item = byMedication[occurrence.medicationId] ?: continue
                val medication = item.medication
                when (occurrence) {
                    is ReminderOccurrence.Dose -> {
                        val at = occurrence.at.toInstant(zone)
                        if (doseLogRepository.getForSlot(medication.id, at) != null) continue
                        val stillAnswerable = now - at < missedAfter
                        doseLogRepository.insert(
                            DoseLog(
                                medicationId = medication.id,
                                scheduleId = occurrence.scheduleId,
                                scheduledAt = at,
                                status = if (stillAnswerable) DoseStatus.PENDING else DoseStatus.MISSED,
                            ),
                        )
                        if (stillAnswerable) {
                            notifier.showDose(
                                DueDose(
                                    medication = medication,
                                    scheduleId = occurrence.scheduleId,
                                    scheduledAt = at,
                                    time = occurrence.at.time,
                                    doseText = item.schedule.doseTextAt(occurrence.at.time, medication.dose),
                                    silent = !settings.soundEnabled,
                                ),
                            )
                        }
                    }
                    is ReminderOccurrence.HeadsUp -> {
                        if (now - occurrence.at.toInstant(zone) < HEADS_UP_WINDOW) {
                            val daysAhead = occurrence.at.date.daysUntil(occurrence.dueAt.date)
                            notifier.showUpcoming(UpcomingDose(medication, occurrence.dueAt, daysAhead))
                        }
                    }
                }
            }
        }
        reminderStateRepository.setLastCycleAt(now)

        for (log in doseLogRepository.getWithStatus(DoseStatus.SNOOZED)) {
            val item = byMedication[log.medicationId]
            if (item == null) {
                doseLogRepository.upsert(log.copy(status = DoseStatus.PENDING))
                continue
            }
            if (snoozeEnd(log, snooze) > now) continue
            doseLogRepository.upsert(log.copy(status = DoseStatus.PENDING, actedAt = now))
            val time = log.scheduledAt.toLocalDateTime(zone).time
            notifier.showDose(
                DueDose(
                    medication = item.medication,
                    scheduleId = log.scheduleId,
                    scheduledAt = log.scheduledAt,
                    time = time,
                    doseText = item.schedule.doseTextAt(time, item.medication.dose),
                    silent = !settings.soundEnabled,
                ),
            )
        }

        for (log in doseLogRepository.getWithStatus(DoseStatus.PENDING)) {
            if (missedDeadline(log, missedAfter) <= now) {
                doseLogRepository.upsert(log.copy(status = DoseStatus.MISSED))
            }
        }

        val candidates = buildList {
            engine.nextAfter(nowLocal, scheduled)?.let { add(it.at.toInstant(zone)) }
            doseLogRepository.getWithStatus(DoseStatus.SNOOZED).forEach { add(snoozeEnd(it, snooze)) }
            doseLogRepository.getWithStatus(DoseStatus.PENDING).forEach { add(missedDeadline(it, missedAfter)) }
        }
        val next = candidates.filter { it > now }.minOrNull()
        if (next != null) scheduler.schedule(ScheduledAlarm(next)) else scheduler.cancel()
    }

    private fun snoozeEnd(log: DoseLog, snooze: Duration): Instant = (log.actedAt ?: log.scheduledAt) + snooze

    private fun missedDeadline(log: DoseLog, missedAfter: Duration): Instant =
        maxOf(log.scheduledAt, log.actedAt ?: log.scheduledAt) + missedAfter

    private companion object {
        val MAX_CATCH_UP = 2.days

        val HEADS_UP_WINDOW = 12.hours
    }
}
