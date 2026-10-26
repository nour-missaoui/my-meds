package com.tunmed.pillswaterreminder.platform.settings

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.tunmed.pillswaterreminder.data.local.settings.SETTINGS_FILE_NAME
import com.tunmed.pillswaterreminder.data.local.settings.createSettingsDataStore

fun createAndroidSettingsDataStore(context: Context): DataStore<Preferences> =
    createSettingsDataStore { context.applicationContext.filesDir.resolve(SETTINGS_FILE_NAME).absolutePath }
