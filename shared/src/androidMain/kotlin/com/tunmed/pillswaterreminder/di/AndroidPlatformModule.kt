package com.tunmed.pillswaterreminder.di

import com.tunmed.pillswaterreminder.data.local.db.buildAppDatabase
import com.tunmed.pillswaterreminder.domain.reminder.ReminderScheduler
import com.tunmed.pillswaterreminder.platform.database.androidDatabaseBuilder
import com.tunmed.pillswaterreminder.platform.reminder.AndroidReminderScheduler
import com.tunmed.pillswaterreminder.platform.settings.createAndroidSettingsDataStore
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.dsl.module

/** Android implementations of the platform ports used by [sharedModules]. */
val androidPlatformModule: Module = module {
    single { buildAppDatabase(androidDatabaseBuilder(androidContext())) }
    single { createAndroidSettingsDataStore(androidContext()) }
    single<ReminderScheduler> { AndroidReminderScheduler(androidContext()) }
}
