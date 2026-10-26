package com.tunmed.pillswaterreminder.domain.model

import kotlin.time.Instant

enum class DoseStatus { PENDING, TAKEN, SKIPPED, MISSED, SNOOZED }

/** What happened to one scheduled dose. */
data class DoseLog(
    val id: Long = 0,
    val medicationId: Long,
    val scheduleId: Long,
    val scheduledAt: Instant,
    val status: DoseStatus,
    val actedAt: Instant? = null,
)
