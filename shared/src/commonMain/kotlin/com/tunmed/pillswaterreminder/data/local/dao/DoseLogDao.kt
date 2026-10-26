package com.tunmed.pillswaterreminder.data.local.dao

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.Query
import androidx.room3.Upsert
import com.tunmed.pillswaterreminder.data.local.entity.DoseLogEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DoseLogDao {
    @Query("SELECT * FROM dose_log WHERE scheduledAtEpochMs >= :fromMs AND scheduledAtEpochMs < :toMs ORDER BY scheduledAtEpochMs")
    fun observeBetween(fromMs: Long, toMs: Long): Flow<List<DoseLogEntity>>

    @Query("SELECT * FROM dose_log WHERE medicationId = :medicationId AND scheduledAtEpochMs = :scheduledAtMs ORDER BY id DESC LIMIT 1")
    suspend fun getForSlot(medicationId: Long, scheduledAtMs: Long): DoseLogEntity?

    @Query("SELECT * FROM dose_log WHERE status = :status ORDER BY scheduledAtEpochMs")
    suspend fun getByStatus(status: String): List<DoseLogEntity>

    @Insert
    suspend fun insert(log: DoseLogEntity): Long

    @Upsert
    suspend fun upsert(log: DoseLogEntity)
}
