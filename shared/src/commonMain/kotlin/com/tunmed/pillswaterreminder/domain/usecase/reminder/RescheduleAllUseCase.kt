package com.tunmed.pillswaterreminder.domain.usecase.reminder

import com.tunmed.pillswaterreminder.core.time.TimeProvider
import com.tunmed.pillswaterreminder.domain.reminder.ReminderScheduler
import com.tunmed.pillswaterreminder.domain.reminder.ScheduleEngine
import com.tunmed.pillswaterreminder.domain.repository.DoseLogRepository
import com.tunmed.pillswaterreminder.domain.repository.MedicationRepository
import com.tunmed.pillswaterreminder.domain.repository.SettingsRepository

/**
 * Re-arms the single next alarm from the database (the source of truth).
 * Runs after every edit, after an alarm fires, on boot / time-zone change, and daily.
 */
class RescheduleAllUseCase(
    private val engine: ScheduleEngine,
    private val medicationRepository: MedicationRepository,
    private val doseLogRepository: DoseLogRepository,
    private val settingsRepository: SettingsRepository,
    private val scheduler: ReminderScheduler,
    private val timeProvider: TimeProvider,
) {
    suspend operator fun invoke() {
        // Phase 2 (after ScheduleEngine is implemented in Phase 1):
        // 1. Mark PENDING doses older than settings.missedAfterMinutes as MISSED (R-3).
        // 2. next = engine.nextAfter(now, medicationRepository.getScheduledMedications())
        // 3. next?.let { scheduler.schedule(ScheduledAlarm(it.at.toInstant(tz))) } ?: scheduler.cancel()
        // Intentionally a no-op for now so boot / worker calls are safe.
    }
}
