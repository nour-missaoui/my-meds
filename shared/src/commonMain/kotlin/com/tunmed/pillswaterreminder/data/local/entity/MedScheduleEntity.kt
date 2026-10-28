package com.tunmed.pillswaterreminder.data.local.entity

import androidx.room3.Entity
import androidx.room3.Index
import androidx.room3.PrimaryKey

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
    val timeDoses: String? = null,
)
