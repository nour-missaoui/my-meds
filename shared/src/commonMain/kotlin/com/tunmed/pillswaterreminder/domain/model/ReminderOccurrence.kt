package com.tunmed.pillswaterreminder.domain.model

import kotlinx.datetime.LocalDateTime

sealed interface ReminderOccurrence {
    val at: LocalDateTime
    val medicationId: Long
    val scheduleId: Long

    data class Dose(
        override val at: LocalDateTime,
        override val medicationId: Long,
        override val scheduleId: Long,
    ) : ReminderOccurrence

    data class HeadsUp(
        override val at: LocalDateTime,
        override val medicationId: Long,
        override val scheduleId: Long,
        val dueAt: LocalDateTime,
    ) : ReminderOccurrence
}
