package com.tunmed.pillswaterreminder.domain.model

import kotlinx.datetime.LocalDateTime

data class MedicationOverview(
    val medication: Medication,
    val schedule: MedSchedule?,
    val nextDoseAt: LocalDateTime?,
)
