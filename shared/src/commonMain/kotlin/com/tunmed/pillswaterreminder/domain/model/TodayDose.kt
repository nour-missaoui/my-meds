package com.tunmed.pillswaterreminder.domain.model

import kotlin.time.Instant
import kotlinx.datetime.LocalTime

data class TodayDose(
    val time: LocalTime,
    val medication: Medication,
    val doseText: String,
    val scheduleId: Long,
    val scheduledAt: Instant,
    val status: DoseStatus?,
    val actedAt: Instant?,
)
