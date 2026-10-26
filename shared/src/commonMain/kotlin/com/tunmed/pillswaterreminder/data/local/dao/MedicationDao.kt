package com.tunmed.pillswaterreminder.data.local.dao

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.Query
import androidx.room3.Update
import com.tunmed.pillswaterreminder.data.local.entity.MedScheduleEntity
import com.tunmed.pillswaterreminder.data.local.entity.MedicationEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MedicationDao {
    @Query("SELECT * FROM medication ORDER BY name COLLATE NOCASE")
    fun observeAll(): Flow<List<MedicationEntity>>

    @Query("SELECT * FROM medication WHERE id = :id")
    fun observeById(id: Long): Flow<MedicationEntity?>

    @Query("SELECT * FROM medication WHERE status = 'ACTIVE'")
    suspend fun getActive(): List<MedicationEntity>

    @Insert
    suspend fun insert(medication: MedicationEntity): Long

    @Update
    suspend fun update(medication: MedicationEntity)

    @Query("UPDATE medication SET status = :status WHERE id = :id")
    suspend fun setStatus(id: Long, status: String)

    @Query("SELECT * FROM med_schedule WHERE medicationId = :medicationId AND validTo IS NULL LIMIT 1")
    suspend fun getCurrentSchedule(medicationId: Long): MedScheduleEntity?

    @Query(
        "SELECT s.* FROM med_schedule s INNER JOIN medication m ON m.id = s.medicationId " +
            "WHERE m.status = 'ACTIVE' AND s.validTo IS NULL"
    )
    suspend fun getActiveSchedules(): List<MedScheduleEntity>

    @Insert
    suspend fun insertSchedule(schedule: MedScheduleEntity): Long

    @Query("UPDATE med_schedule SET validTo = :validTo WHERE id = :id")
    suspend fun closeSchedule(id: Long, validTo: String)
}
