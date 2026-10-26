package com.tunmed.pillswaterreminder.platform.database

import android.content.Context
import androidx.room3.Room
import androidx.room3.RoomDatabase
import com.tunmed.pillswaterreminder.data.local.db.AppDatabase
import com.tunmed.pillswaterreminder.data.local.db.DATABASE_NAME

fun androidDatabaseBuilder(context: Context): RoomDatabase.Builder<AppDatabase> {
    val appContext = context.applicationContext
    val dbFile = appContext.getDatabasePath(DATABASE_NAME)
    return Room.databaseBuilder<AppDatabase>(
        context = appContext,
        name = dbFile.absolutePath,
    )
}
