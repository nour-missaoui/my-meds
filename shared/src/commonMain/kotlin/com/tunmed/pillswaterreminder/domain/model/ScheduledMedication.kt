package com.tunmed.pillswaterreminder.domain.model

data class ScheduledMedication(
    val medication: Medication,
    val schedule: MedSchedule,
)
