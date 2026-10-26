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

@Suppress("KotlinNoActualForExpect")
expect object AppDatabaseConstructor : RoomDatabaseConstructor<AppDatabase> {
    override fun initialize(): AppDatabase
}
