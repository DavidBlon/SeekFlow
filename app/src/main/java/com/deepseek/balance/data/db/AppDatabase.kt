package com.deepseek.balance.data.db

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [UsageEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun usageDao(): UsageDao
}
