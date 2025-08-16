package com.omnixone.hopeapp.db.dao

import androidx.room.*

import com.omnixone.hopeapp.db.entity.TaskSessionEntity

@Dao
interface TaskSessionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: TaskSessionEntity)

    // Update session by taskUuid and latest startTime (find session with no endTime)
    @Query("""
        UPDATE task_sessions 
        SET endTime = :endTime, duration = :duration 
        WHERE taskUuid = :taskUuid AND endTime IS NULL
    """)
    suspend fun updateEndTimeAndDuration(taskUuid: String, endTime: Long, duration: Long)

    @Query("SELECT * FROM task_sessions WHERE date = :date")
    fun getSessionsByDate(date: String): List<TaskSessionEntity>



}
