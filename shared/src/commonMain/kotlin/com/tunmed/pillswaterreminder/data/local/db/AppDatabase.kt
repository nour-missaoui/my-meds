package com.tunmed.pillswaterreminder.data.local.db

import androidx.room3.ConstructedBy
import androidx.room3.Database
import androidx.room3.RoomDatabase
import androidx.room3.RoomDatabaseConstructor
import com.tunmed.pillswaterreminder.data.local.dao.DoseLogDao
import com.tunmed.pillswaterreminder.data.local.dao.MedicationDao
import com.tunmed.pillswaterreminder.data.local.entity.DoseLogEntity
import com.tunmed.pillswaterreminder.data.local.entity.MedScheduleEntity
import com.tunmed.pillswaterreminder.data.local.entity.MedicationEntity

/**
 * Version 1 has never shipped, so it may still change freely. After the first Play release,
 * every change needs a version bump and a migration (schemas are exported to shared/schemas).
 */
@Database(
    entities = [
        MedicationEntity::class,
        MedScheduleEntity::class,
        DoseLogEntity::class,
    ],
    version = 1,
    exportSchema = true,
)
@ConstructedBy(AppDatabaseConstructor::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun medicationDao(): MedicationDao
    abstract fun doseLogDao(): DoseLogDao
}

// The Room compiler (KSP) generates the `actual` implementations for each target.
@Suppress("KotlinNoActualForExpect")
expect object AppDatabaseConstructor : RoomDatabaseConstructor<AppDatabase> {
    override fun initialize(): AppDatabase
}
