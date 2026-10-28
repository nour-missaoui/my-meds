package com.tunmed.pillswaterreminder.data.local.entity

import androidx.room3.Entity
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
    val doseAmount: Double? = null,
    val doseUnit: String? = null,
    val strength: String? = null,
)
