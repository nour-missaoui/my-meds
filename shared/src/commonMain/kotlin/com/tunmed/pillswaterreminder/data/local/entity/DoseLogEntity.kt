package com.tunmed.pillswaterreminder.data.local.entity

import androidx.room3.Entity
import androidx.room3.Index
import androidx.room3.PrimaryKey

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
