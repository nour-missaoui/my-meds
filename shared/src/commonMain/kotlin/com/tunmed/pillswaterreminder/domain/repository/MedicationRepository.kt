package com.tunmed.pillswaterreminder.domain.repository

import com.tunmed.pillswaterreminder.domain.model.MedSchedule
import com.tunmed.pillswaterreminder.domain.model.Medication
import com.tunmed.pillswaterreminder.domain.model.MedicationStatus
import com.tunmed.pillswaterreminder.domain.model.ScheduledMedication
import kotlinx.coroutines.flow.Flow

interface MedicationRepository {
    fun observeMedications(): Flow<List<Medication>>
    fun observeMedication(id: Long): Flow<Medication?>

    suspend fun getCurrentSchedule(medicationId: Long): MedSchedule?

    suspend fun getScheduledMedications(): List<ScheduledMedication>

    fun observeScheduledMedications(): Flow<List<ScheduledMedication>>

    suspend fun save(medication: Medication, schedule: MedSchedule): Long

    suspend fun setStatus(id: Long, status: MedicationStatus)
}
