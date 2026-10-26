package com.tunmed.pillswaterreminder.domain.repository

import com.tunmed.pillswaterreminder.domain.model.MedSchedule
import com.tunmed.pillswaterreminder.domain.model.Medication
import com.tunmed.pillswaterreminder.domain.model.MedicationStatus
import com.tunmed.pillswaterreminder.domain.model.ScheduledMedication
import kotlinx.coroutines.flow.Flow

interface MedicationRepository {
    fun observeMedications(): Flow<List<Medication>>
    fun observeMedication(id: Long): Flow<Medication?>

    /** The open (validTo == null) schedule version of a medication. */
    suspend fun getCurrentSchedule(medicationId: Long): MedSchedule?

    /** ACTIVE medications with their open schedule version; input for the reminder engine. */
    suspend fun getScheduledMedications(): List<ScheduledMedication>

    /**
     * Inserts or updates [medication] and starts a new schedule version from
     * [MedSchedule.validFrom], closing the previous one (spec M-2). Returns the medication id.
     */
    suspend fun save(medication: Medication, schedule: MedSchedule): Long

    suspend fun setStatus(id: Long, status: MedicationStatus)
}
