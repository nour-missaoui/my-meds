package com.tunmed.pillswaterreminder.domain.repository

import com.tunmed.pillswaterreminder.domain.model.AppSettings
import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    fun observe(): Flow<AppSettings>
    suspend fun update(transform: (AppSettings) -> AppSettings)
}
