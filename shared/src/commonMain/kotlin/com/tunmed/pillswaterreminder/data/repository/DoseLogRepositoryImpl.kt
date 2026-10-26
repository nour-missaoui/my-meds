package com.tunmed.pillswaterreminder.data.repository

import com.tunmed.pillswaterreminder.data.local.dao.DoseLogDao
import com.tunmed.pillswaterreminder.data.mapper.toDomain
import com.tunmed.pillswaterreminder.data.mapper.toEntity
import com.tunmed.pillswaterreminder.domain.model.DoseLog
import com.tunmed.pillswaterreminder.domain.repository.DoseLogRepository
import kotlin.time.Instant
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class DoseLogRepositoryImpl(
    private val dao: DoseLogDao,
) : DoseLogRepository {

    override fun observeBetween(from: Instant, to: Instant): Flow<List<DoseLog>> =
        dao.observeBetween(from.toEpochMilliseconds(), to.toEpochMilliseconds())
            .map { list -> list.map { it.toDomain() } }

    override suspend fun upsert(log: DoseLog) {
        dao.upsert(log.toEntity())
    }

    override suspend fun getPendingBefore(instant: Instant): List<DoseLog> =
        dao.getPendingBefore(instant.toEpochMilliseconds()).map { it.toDomain() }
}
