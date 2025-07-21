package com.omnixone.hopeapp.db.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "task_sessions",
    foreignKeys = [
        ForeignKey(
            entity = TaskEntity::class,
            parentColumns = ["uuid"],
            childColumns = ["taskUuid"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["taskUuid"])] // important for performance
)
data class TaskSessionEntity(
    @PrimaryKey(autoGenerate = true) val uuid: Int = 0,

    val taskUuid: String, // Foreign key reference to TaskEntity (converted to String for consistency with SharedPref)

    val date: String,     // Format: yyyy-MM-dd
    val startTime: Long,  // Epoch time in milliseconds
    val endTime: Long?,   // Nullable - will be updated later
    val duration: Long?   // Nullable - will be calculated when task stops
)
