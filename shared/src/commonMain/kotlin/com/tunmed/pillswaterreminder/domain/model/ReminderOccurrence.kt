package com.tunmed.pillswaterreminder.domain.model

import kotlinx.datetime.LocalDateTime

/** One computed reminder (never stored): the output of ScheduleEngine. */
sealed interface ReminderOccurrence {
    val at: LocalDateTime
    val medicationId: Long
    val scheduleId: Long

    /** A dose is due at [at] (spec R-1). */
    data class Dose(
        override val at: LocalDateTime,
        override val medicationId: Long,
        override val scheduleId: Long,
    ) : ReminderOccurrence

    /** Early warning for a monthly / yearly dose due at [dueAt] (spec S-7). */
    data class HeadsUp(
        override val at: LocalDateTime,
        override val medicationId: Long,
        override val scheduleId: Long,
        val dueAt: LocalDateTime,
    ) : ReminderOccurrence
}
