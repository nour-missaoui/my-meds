package com.tunmed.pillswaterreminder.domain.usecase.reminder

import com.tunmed.pillswaterreminder.core.time.TimeProvider
import com.tunmed.pillswaterreminder.domain.model.AppSettings
import com.tunmed.pillswaterreminder.domain.model.DoseLog
import com.tunmed.pillswaterreminder.domain.model.DoseStatus
import com.tunmed.pillswaterreminder.domain.model.MedSchedule
import com.tunmed.pillswaterreminder.domain.model.Medication
import com.tunmed.pillswaterreminder.domain.model.MedicationForm
import com.tunmed.pillswaterreminder.domain.model.MedicationStatus
import com.tunmed.pillswaterreminder.domain.model.RecurrenceRule
import com.tunmed.pillswaterreminder.domain.model.ScheduledMedication
import com.tunmed.pillswaterreminder.domain.model.TimeDose
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
import com.tunmed.pillswaterreminder.domain.usecase.dose.RecordDoseActionUseCase
import com.tunmed.pillswaterreminder.domain.usecase.medication.DeleteMedicationUseCase
import com.tunmed.pillswaterreminder.domain.usecase.medication.SaveMedicationUseCase
import com.tunmed.pillswaterreminder.domain.usecase.medication.SetMedicationStatusUseCase
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Instant
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant

class ReminderCycleTest {

    @Test
    fun first_run_only_sets_the_watermark_and_arms_the_next_dose() = runTest {
        val env = Env()
        env.runAt(at(7, 0))
        assertTrue(env.logs.all().isEmpty())
        assertEquals(at(7, 0), env.state.lastCycleAt)
        assertEquals(at(8, 0), env.scheduler.armedAt)
    }

    @Test
    fun r1_due_dose_gets_a_pending_log_and_a_notification() = runTest {
        val env = Env()
        env.runAt(at(7, 0))
        env.runAt(at(8, 0))
        assertEquals(DoseStatus.PENDING, env.logs.at(at(8, 0))?.status)
        assertEquals(listOf(at(8, 0)), env.notifier.shown.map { it.scheduledAt })
        assertEquals(at(9, 0), env.scheduler.armedAt, "next wake-up is the missed deadline, before 20:00")
    }

    @Test
    fun r2_snooze_shows_the_dose_again_after_10_minutes() = runTest {
        val env = Env()
        env.runAt(at(7, 0))
        env.runAt(at(8, 0))
        env.time.current = at(8, 2)
        env.record(MED_ID, SCHEDULE_ID, at(8, 0), DoseStatus.SNOOZED)
        assertEquals(DoseStatus.SNOOZED, env.logs.at(at(8, 0))?.status)
        assertEquals(listOf(MED_ID to at(8, 0)), env.notifier.cancelled)
        assertEquals(at(8, 12), env.scheduler.armedAt)

        env.runAt(at(8, 12))
        val log = env.logs.at(at(8, 0))
        assertEquals(DoseStatus.PENDING, log?.status)
        assertEquals(at(8, 12), log?.actedAt)
        assertEquals(2, env.notifier.shown.size)
        assertEquals(at(9, 12), env.scheduler.armedAt, "missed deadline counts from when it was shown again")
    }

    @Test
    fun r3_unanswered_dose_becomes_missed_after_60_minutes() = runTest {
        val env = Env()
        env.runAt(at(7, 0))
        env.runAt(at(8, 0))
        env.runAt(at(9, 0))
        assertEquals(DoseStatus.MISSED, env.logs.at(at(8, 0))?.status)
        assertEquals(at(20, 0), env.scheduler.armedAt)
    }

    @Test
    fun r6_dose_due_while_the_phone_was_off_for_too_long_is_missed_without_notification() = runTest {
        val env = Env()
        env.runAt(at(7, 0))
        env.runAt(at(10, 0))
        assertEquals(DoseStatus.MISSED, env.logs.at(at(8, 0))?.status)
        assertTrue(env.notifier.shown.isEmpty())
    }

    @Test
    fun r6_dose_due_while_the_phone_was_off_briefly_is_still_shown() = runTest {
        val env = Env()
        env.runAt(at(7, 0))
        env.runAt(at(8, 30))
        assertEquals(DoseStatus.PENDING, env.logs.at(at(8, 0))?.status)
        assertEquals(1, env.notifier.shown.size)
        assertEquals(at(9, 0), env.scheduler.armedAt)
    }

    @Test
    fun r7_late_taken_records_the_real_time() = runTest {
        val env = Env()
        env.runAt(at(7, 0))
        env.runAt(at(8, 0))
        env.runAt(at(9, 0))
        env.time.current = at(10, 30)
        env.record(MED_ID, SCHEDULE_ID, at(8, 0), DoseStatus.TAKEN)
        val log = env.logs.at(at(8, 0))
        assertEquals(DoseStatus.TAKEN, log?.status)
        assertEquals(at(10, 30), log?.actedAt)
        assertEquals(1, env.logs.all().size, "the existing log is updated, not duplicated")
    }

    @Test
    fun taken_early_means_no_reminder_at_dose_time() = runTest {
        val env = Env()
        env.runAt(at(7, 0))
        env.time.current = at(7, 30)
        env.record(MED_ID, SCHEDULE_ID, at(8, 0), DoseStatus.TAKEN)
        env.runAt(at(8, 0))
        assertEquals(DoseStatus.TAKEN, env.logs.at(at(8, 0))?.status)
        assertTrue(env.notifier.shown.isEmpty())
        assertEquals(at(20, 0), env.scheduler.armedAt)
    }

    @Test
    fun medication_added_after_its_dose_time_is_not_marked_missed() = runTest {
        val env = Env(initial = emptyList())
        env.runAt(at(7, 0, day = 25))
        env.time.current = at(15, 0)
        SaveMedicationUseCase(env.meds, env.cycle)(MEDICATION.copy(id = 0), SCHEDULE)
        assertTrue(env.logs.all().isEmpty(), "doses before the medication was added are not logged")
        assertEquals(at(20, 0), env.scheduler.armedAt)
    }

    @Test
    fun paused_medication_gets_no_reminders_and_no_alarm() = runTest {
        val env = Env(initial = listOf(ScheduledMedication(MEDICATION.copy(status = MedicationStatus.PAUSED), SCHEDULE)))
        env.runAt(at(7, 0))
        env.runAt(at(8, 0))
        assertTrue(env.logs.all().isEmpty())
        assertNull(env.scheduler.armedAt)
    }

    @Test
    fun m3_deleted_medication_keeps_its_history_and_gets_no_more_reminders() = runTest {
        val env = Env()
        env.runAt(at(7, 0))
        env.runAt(at(8, 0))
        env.time.current = at(8, 2)
        env.record(MED_ID, SCHEDULE_ID, at(8, 0), DoseStatus.TAKEN)
        env.time.current = at(8, 30)
        env.delete(MED_ID)
        assertEquals(listOf(MED_ID), env.notifier.cancelledMedications)
        assertNull(env.scheduler.armedAt, "nothing left to remind")

        env.runAt(at(20, 0))
        assertEquals(1, env.logs.all().size, "no new dose at 20:00")
        assertEquals(DoseStatus.TAKEN, env.logs.at(at(8, 0))?.status, "history kept")
    }

    @Test
    fun m3_snoozed_dose_of_a_deleted_medication_ends_as_missed() = runTest {
        val env = Env()
        env.runAt(at(7, 0))
        env.runAt(at(8, 0))
        env.time.current = at(8, 2)
        env.record(MED_ID, SCHEDULE_ID, at(8, 0), DoseStatus.SNOOZED)
        env.time.current = at(8, 5)
        env.delete(MED_ID)
        assertEquals(DoseStatus.PENDING, env.logs.at(at(8, 0))?.status)
        assertEquals(at(9, 2), env.scheduler.armedAt)

        env.runAt(at(9, 2))
        assertEquals(DoseStatus.MISSED, env.logs.at(at(8, 0))?.status)
        assertEquals(1, env.notifier.shown.size, "never shown again after the delete")
        assertNull(env.scheduler.armedAt)
    }

    @Test
    fun s1_the_dose_can_differ_by_time_of_day() = runTest {
        val schedule = SCHEDULE.copy(doseByTime = mapOf(LocalTime(8, 0) to TimeDose(text = "2 tablets", amount = 2.0)))
        val env = Env(initial = listOf(ScheduledMedication(MEDICATION, schedule)))
        env.runAt(at(7, 0))
        env.runAt(at(8, 0))
        env.runAt(at(20, 0))
        assertEquals(listOf("2 tablets", "500 mg"), env.notifier.shown.map { it.doseText }, "08:00 has its own dose, 20:00 uses the medication's")
    }

    private class Env(initial: List<ScheduledMedication> = listOf(ScheduledMedication(MEDICATION, SCHEDULE))) {
        val time = FakeTimeProvider(at(0, 0))
        val meds = FakeMedicationRepository(initial)
        val logs = FakeDoseLogRepository()
        val state = FakeReminderStateRepository()
        val scheduler = FakeScheduler()
        val notifier = FakeNotifier()
        val cycle = RescheduleAllUseCase(
            engine = ScheduleEngine(),
            medicationRepository = meds,
            doseLogRepository = logs,
            settingsRepository = FakeSettingsRepository(),
            reminderStateRepository = state,
            scheduler = scheduler,
            notifier = notifier,
            timeProvider = time,
        )
        val record = RecordDoseActionUseCase(logs, notifier, time, cycle)
        val delete = DeleteMedicationUseCase(SetMedicationStatusUseCase(meds, cycle), notifier)

        suspend fun runAt(instant: Instant) {
            time.current = instant
            cycle()
        }
    }

    private companion object {
        const val MED_ID = 1L
        const val SCHEDULE_ID = 10L
        val MEDICATION = Medication(
            id = MED_ID,
            name = "Metformin",
            dose = "500 mg",
            form = MedicationForm.TABLET,
            startDate = LocalDate(2026, 10, 1),
        )
        val SCHEDULE = MedSchedule(
            id = SCHEDULE_ID,
            medicationId = MED_ID,
            rule = RecurrenceRule.Daily,
            times = listOf(LocalTime(8, 0), LocalTime(20, 0)),
            validFrom = LocalDate(2026, 10, 1),
        )

        fun at(hour: Int, minute: Int, day: Int = 26): Instant =
            LocalDateTime(2026, 10, day, hour, minute).toInstant(TimeZone.UTC)
    }
}

private class FakeTimeProvider(var current: Instant) : TimeProvider {
    override fun now(): Instant = current
    override fun timeZone(): TimeZone = TimeZone.UTC
}

private class FakeMedicationRepository(initial: List<ScheduledMedication>) : MedicationRepository {
    private val items = MutableStateFlow(initial)

    override fun observeMedications(): Flow<List<Medication>> = items.map { list -> list.map { it.medication } }

    override fun observeMedication(id: Long): Flow<Medication?> =
        items.map { list -> list.firstOrNull { it.medication.id == id }?.medication }

    override suspend fun getCurrentSchedule(medicationId: Long): MedSchedule? =
        items.value.firstOrNull { it.medication.id == medicationId }?.schedule

    override suspend fun getScheduledMedications(): List<ScheduledMedication> =
        items.value.filter { it.medication.status == MedicationStatus.ACTIVE }

    override fun observeScheduledMedications(): Flow<List<ScheduledMedication>> =
        items.map { list -> list.filter { it.medication.status == MedicationStatus.ACTIVE } }

    override suspend fun save(medication: Medication, schedule: MedSchedule): Long {
        val id = if (medication.id == 0L) (items.value.maxOfOrNull { it.medication.id } ?: 0L) + 1 else medication.id
        val saved = ScheduledMedication(medication.copy(id = id), schedule.copy(id = id * 10, medicationId = id))
        items.value = items.value.filterNot { it.medication.id == id } + saved
        return id
    }

    override suspend fun setStatus(id: Long, status: MedicationStatus) {
        items.value = items.value.map {
            if (it.medication.id == id) ScheduledMedication(it.medication.copy(status = status), it.schedule) else it
        }
    }
}

private class FakeDoseLogRepository : DoseLogRepository {
    private val logs = MutableStateFlow<List<DoseLog>>(emptyList())
    private var nextId = 1L

    fun all(): List<DoseLog> = logs.value
    fun at(scheduledAt: Instant): DoseLog? = logs.value.singleOrNull { it.scheduledAt == scheduledAt }

    override fun observeBetween(from: Instant, to: Instant): Flow<List<DoseLog>> =
        logs.map { list -> list.filter { it.scheduledAt >= from && it.scheduledAt < to } }

    override suspend fun getForSlot(medicationId: Long, scheduledAt: Instant): DoseLog? =
        logs.value.lastOrNull { it.medicationId == medicationId && it.scheduledAt == scheduledAt }

    override suspend fun getWithStatus(status: DoseStatus): List<DoseLog> = logs.value.filter { it.status == status }

    override suspend fun insert(log: DoseLog): Long {
        val id = nextId++
        logs.value = logs.value + log.copy(id = id)
        return id
    }

    override suspend fun upsert(log: DoseLog) {
        if (log.id == 0L) insert(log) else logs.value = logs.value.map { if (it.id == log.id) log else it }
    }
}

private class FakeSettingsRepository : SettingsRepository {
    private val settings = MutableStateFlow(AppSettings())
    override fun observe(): Flow<AppSettings> = settings
    override suspend fun update(transform: (AppSettings) -> AppSettings) {
        settings.value = transform(settings.value)
    }
}

private class FakeReminderStateRepository : ReminderStateRepository {
    var lastCycleAt: Instant? = null
    override suspend fun getLastCycleAt(): Instant? = lastCycleAt
    override suspend fun setLastCycleAt(at: Instant) {
        lastCycleAt = at
    }
}

private class FakeScheduler : ReminderScheduler {
    var armedAt: Instant? = null
    override fun canScheduleExactAlarms(): Boolean = true
    override fun schedule(alarm: ScheduledAlarm) {
        armedAt = alarm.at
    }
    override fun cancel() {
        armedAt = null
    }
}

private class FakeNotifier : ReminderNotifier {
    val shown = mutableListOf<DueDose>()
    val upcoming = mutableListOf<UpcomingDose>()
    val cancelled = mutableListOf<Pair<Long, Instant>>()
    val cancelledMedications = mutableListOf<Long>()
    override fun showDose(dose: DueDose) {
        shown += dose
    }
    override fun showUpcoming(upcoming: UpcomingDose) {
        this.upcoming += upcoming
    }
    override fun cancelDose(medicationId: Long, scheduledAt: Instant) {
        cancelled += medicationId to scheduledAt
    }
    override fun cancelMedication(medicationId: Long) {
        cancelledMedications += medicationId
    }
}
