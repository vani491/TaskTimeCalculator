package com.omnixone.hopeapp.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import com.omnixone.hopeapp.db.dao.TaskDao
import com.omnixone.hopeapp.db.dao.TaskSessionDao

import com.omnixone.hopeapp.db.entity.TaskEntity
import com.omnixone.hopeapp.db.entity.TaskSessionEntity

import java.util.Date

@Database(entities = [ TaskEntity::class, TaskSessionEntity::class ], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    abstract fun taskDao(): TaskDao
    abstract fun taskSessionDao(): TaskSessionDao


    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "task_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}

// Type converters if needed
class Converters {
    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }
}
