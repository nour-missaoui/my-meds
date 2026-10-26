package com.tunmed.pillswaterreminder.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import com.tunmed.pillswaterreminder.domain.repository.ReminderStateRepository
import kotlin.time.Instant
import kotlinx.coroutines.flow.first

class ReminderStateRepositoryImpl(
    private val dataStore: DataStore<Preferences>,
) : ReminderStateRepository {

    override suspend fun getLastCycleAt(): Instant? =
        dataStore.data.first()[LAST_CYCLE_AT]?.let { Instant.fromEpochMilliseconds(it) }

    override suspend fun setLastCycleAt(at: Instant) {
        dataStore.edit { it[LAST_CYCLE_AT] = at.toEpochMilliseconds() }
    }

    private companion object {
        val LAST_CYCLE_AT = longPreferencesKey("last_reminder_cycle_at")
    }
}
