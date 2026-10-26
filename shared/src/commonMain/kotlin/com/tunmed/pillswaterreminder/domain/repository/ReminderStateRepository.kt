package com.tunmed.pillswaterreminder.domain.repository

import kotlin.time.Instant

interface ReminderStateRepository {
    suspend fun getLastCycleAt(): Instant?
    suspend fun setLastCycleAt(at: Instant)
}
