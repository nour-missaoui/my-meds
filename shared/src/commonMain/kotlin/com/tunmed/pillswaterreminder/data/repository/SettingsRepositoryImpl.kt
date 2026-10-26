package com.tunmed.pillswaterreminder.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import com.tunmed.pillswaterreminder.domain.model.AppSettings
import com.tunmed.pillswaterreminder.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class SettingsRepositoryImpl(
    private val dataStore: DataStore<Preferences>,
) : SettingsRepository {

    override fun observe(): Flow<AppSettings> = dataStore.data.map { it.toAppSettings() }

    override suspend fun update(transform: (AppSettings) -> AppSettings) {
        dataStore.edit { prefs -> prefs.write(transform(prefs.toAppSettings())) }
    }

    private object Keys {
        val ONBOARDING_DONE = booleanPreferencesKey("onboarding_done")
        val SNOOZE_MINUTES = intPreferencesKey("snooze_minutes")
        val MISSED_AFTER_MINUTES = intPreferencesKey("missed_after_minutes")
        val SOUND = booleanPreferencesKey("sound_enabled")
        val VIBRATION = booleanPreferencesKey("vibration_enabled")
    }

    private fun Preferences.toAppSettings(): AppSettings {
        val defaults = AppSettings()
        return AppSettings(
            onboardingDone = this[Keys.ONBOARDING_DONE] ?: defaults.onboardingDone,
            snoozeMinutes = this[Keys.SNOOZE_MINUTES] ?: defaults.snoozeMinutes,
            missedAfterMinutes = this[Keys.MISSED_AFTER_MINUTES] ?: defaults.missedAfterMinutes,
            soundEnabled = this[Keys.SOUND] ?: defaults.soundEnabled,
            vibrationEnabled = this[Keys.VIBRATION] ?: defaults.vibrationEnabled,
        )
    }

    private fun MutablePreferences.write(settings: AppSettings) {
        this[Keys.ONBOARDING_DONE] = settings.onboardingDone
        this[Keys.SNOOZE_MINUTES] = settings.snoozeMinutes
        this[Keys.MISSED_AFTER_MINUTES] = settings.missedAfterMinutes
        this[Keys.SOUND] = settings.soundEnabled
        this[Keys.VIBRATION] = settings.vibrationEnabled
    }
}
