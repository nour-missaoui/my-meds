package com.tunmed.pillswaterreminder.domain.repository

import com.tunmed.pillswaterreminder.domain.model.MedSchedule
import com.tunmed.pillswaterreminder.domain.model.Medication
import com.tunmed.pillswaterreminder.domain.model.MedicationStatus
import com.tunmed.pillswaterreminder.domain.model.ScheduledMedication
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate

interface MedicationRepository {
    fun observeMedications(): Flow<List<Medication>>
    fun observeMedication(id: Long): Flow<Medication?>

    suspend fun getCurrentSchedule(medicationId: Long): MedSchedule?

    suspend fun getScheduledMedications(): List<ScheduledMedication>

    fun observeScheduledMedications(): Flow<List<ScheduledMedication>>

    fun observeCurrentSchedules(): Flow<List<MedSchedule>>

    suspend fun save(medication: Medication, schedule: MedSchedule): Long

    suspend fun setStatus(id: Long, status: MedicationStatus)

    suspend fun archiveEndedBefore(date: LocalDate)

    suspend fun restore(id: Long, today: LocalDate)
}
