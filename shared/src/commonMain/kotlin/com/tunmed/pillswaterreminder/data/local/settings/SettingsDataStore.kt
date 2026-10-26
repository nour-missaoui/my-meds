package com.tunmed.pillswaterreminder.data.local.settings

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import okio.Path.Companion.toPath

/** Must end with ".preferences_pb". */
const val SETTINGS_FILE_NAME = "settings.preferences_pb"

/** Create exactly one instance per process (it is a Koin `single`). */
fun createSettingsDataStore(producePath: () -> String): DataStore<Preferences> =
    PreferenceDataStoreFactory.createWithPath(produceFile = { producePath().toPath() })
