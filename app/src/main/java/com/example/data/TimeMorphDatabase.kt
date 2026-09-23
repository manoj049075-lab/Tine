package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [DailyEntry::class, TimeCapsule::class, Milestone::class],
    version = 1,
    exportSchema = false
)
abstract class TimeMorphDatabase : RoomDatabase() {
    abstract fun dailyEntryDao(): DailyEntryDao
    abstract fun timeCapsuleDao(): TimeCapsuleDao
    abstract fun milestoneDao(): MilestoneDao

    companion object {
        @Volatile
        private var INSTANCE: TimeMorphDatabase? = null

        fun getDatabase(context: Context): TimeMorphDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    TimeMorphDatabase::class.java,
                    "timemorph_db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
