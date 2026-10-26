package com.tunmed.pillswaterreminder.data.local.entity

import androidx.room3.Entity
import androidx.room3.Index
import androidx.room3.PrimaryKey

@Entity(tableName = "medication")
data class MedicationEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val dose: String,
    val form: String,
    val note: String?,
    val color: String,
    val startDate: String,
    val endDate: String?,
    val status: String,
)

/**
 * Versioned schedule. [ruleType] selects which nullable rule columns are used
 * (see ScheduleMappers). Weekdays are ISO numbers "1,3,5" (Monday = 1).
 */
@Entity(tableName = "med_schedule", indices = [Index(value = ["medicationId"])])
data class MedScheduleEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val medicationId: Long,
    val ruleType: String,
    val times: String,
    val everyNDays: Int? = null,
    val anchorDate: String? = null,
    val weekdays: String? = null,
    val dayOfMonth: Int? = null,
    val weekOfMonth: Int? = null,
    val weekday: Int? = null,
    val month: Int? = null,
    val headsUpDays: Int = 0,
    val validFrom: String,
    val validTo: String? = null,
)

@Entity(
    tableName = "dose_log",
    indices = [Index(value = ["scheduleId", "scheduledAtEpochMs"], unique = true)],
)
data class DoseLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val medicationId: Long,
    val scheduleId: Long,
    val scheduledAtEpochMs: Long,
    val status: String,
    val actedAtEpochMs: Long?,
)
