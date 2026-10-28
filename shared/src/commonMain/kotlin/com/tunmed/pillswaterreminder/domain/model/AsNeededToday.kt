package com.tunmed.pillswaterreminder.domain.model

import kotlin.time.Instant

data class AsNeededToday(
    val medication: Medication,
    val takenCount: Int,
    val lastTakenAt: Instant?,
)
