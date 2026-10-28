package com.tunmed.pillswaterreminder.domain.reminder

import com.tunmed.pillswaterreminder.domain.model.Medication
import kotlin.time.Instant
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime

data class DueDose(
    val medication: Medication,
    val scheduleId: Long,
    val scheduledAt: Instant,
    val time: LocalTime,
    val doseText: String,
    val silent: Boolean = false,
    val vibrate: Boolean = true,
)

data class UpcomingDose(
    val medication: Medication,
    val dueAt: LocalDateTime,
    val daysAhead: Int,
)

interface ReminderNotifier {
    fun showDose(dose: DueDose)
    fun showUpcoming(upcoming: UpcomingDose)
    fun cancelDose(medicationId: Long, scheduledAt: Instant)

    fun cancelMedication(medicationId: Long)
}
