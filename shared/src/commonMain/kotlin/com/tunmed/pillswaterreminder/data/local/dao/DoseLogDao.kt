package com.tunmed.pillswaterreminder.data.local.dao

import androidx.room3.Dao
import androidx.room3.Query
import androidx.room3.Upsert
import com.tunmed.pillswaterreminder.data.local.entity.DoseLogEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DoseLogDao {
    @Query("SELECT * FROM dose_log WHERE scheduledAtEpochMs >= :fromMs AND scheduledAtEpochMs < :toMs ORDER BY scheduledAtEpochMs")
    fun observeBetween(fromMs: Long, toMs: Long): Flow<List<DoseLogEntity>>

    @Upsert
    suspend fun upsert(log: DoseLogEntity)

    @Query("SELECT * FROM dose_log WHERE status = 'PENDING' AND scheduledAtEpochMs < :beforeMs")
    suspend fun getPendingBefore(beforeMs: Long): List<DoseLogEntity>
}
