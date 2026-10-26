package com.tunmed.pillswaterreminder.domain.repository

import com.tunmed.pillswaterreminder.domain.model.DoseLog
import com.tunmed.pillswaterreminder.domain.model.DoseStatus
import kotlin.time.Instant
import kotlinx.coroutines.flow.Flow

interface DoseLogRepository {
    fun observeBetween(from: Instant, to: Instant): Flow<List<DoseLog>>

    suspend fun getForSlot(medicationId: Long, scheduledAt: Instant): DoseLog?

    suspend fun getWithStatus(status: DoseStatus): List<DoseLog>

    suspend fun insert(log: DoseLog): Long

    suspend fun upsert(log: DoseLog)
}
