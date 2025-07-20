package com.omnixone.hopeapp.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey(autoGenerate = true) val uuid: Int = 0,
    val index: Int,
    val title: String,
    val description: String,
    val totalHours: Int
)