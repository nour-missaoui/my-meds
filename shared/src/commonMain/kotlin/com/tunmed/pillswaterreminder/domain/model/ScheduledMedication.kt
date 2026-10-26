package com.tunmed.pillswaterreminder.domain.model

/**
 * An ACTIVE medication with its current schedule version: the input of ScheduleEngine.
 * The engine needs both: the rule and times from [schedule], the start / end dates from [medication].
 */
data class ScheduledMedication(
    val medication: Medication,
    val schedule: MedSchedule,
)
