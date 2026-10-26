package com.tunmed.pillswaterreminder.domain.repository

import com.tunmed.pillswaterreminder.domain.model.DoseLog
import kotlin.time.Instant
import kotlinx.coroutines.flow.Flow

interface DoseLogRepository {
    fun observeBetween(from: Instant, to: Instant): Flow<List<DoseLog>>
    suspend fun upsert(log: DoseLog)

    /** PENDING doses scheduled before [instant]; used to mark them MISSED. */
    suspend fun getPendingBefore(instant: Instant): List<DoseLog>
}
