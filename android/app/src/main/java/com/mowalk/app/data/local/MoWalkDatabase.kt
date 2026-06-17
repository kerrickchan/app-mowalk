package com.mowalk.app.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [DailyStepEntity::class, UserProfileEntity::class],
    version = 1,
    exportSchema = true
)
abstract class MoWalkDatabase : RoomDatabase() {

    abstract fun stepDao(): StepDao

    companion object {
        private const val DATABASE_NAME = "mowalk_db"

        @Volatile
        private var INSTANCE: MoWalkDatabase? = null

        fun getDatabase(context: Context): MoWalkDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    MoWalkDatabase::class.java,
                    DATABASE_NAME
                )
                    .fallbackToDestructiveMigration(false)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
