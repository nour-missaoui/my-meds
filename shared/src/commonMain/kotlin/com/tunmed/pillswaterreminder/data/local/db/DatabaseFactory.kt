package com.tunmed.pillswaterreminder.data.local.db

import androidx.room3.RoomDatabase
import androidx.sqlite.driver.bundled.BundledSQLiteDriver

/** Renamed from "pills_water.db" when water was dropped, so old debug installs start fresh. */
const val DATABASE_NAME = "meds_reminder.db"

/**
 * Finishes the platform-specific builder (see platform/database in androidMain).
 * Queries run on Dispatchers.IO by default.
 */
fun buildAppDatabase(builder: RoomDatabase.Builder<AppDatabase>): AppDatabase =
    builder
        .setDriver(BundledSQLiteDriver())
        .build()
