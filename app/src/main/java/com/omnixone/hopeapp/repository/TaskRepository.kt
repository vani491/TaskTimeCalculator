package com.omnixone.hopeapp.repository


import com.omnixone.hopeapp.db.dao.TaskDao
import com.omnixone.hopeapp.db.dao.TaskSessionDao
import com.omnixone.hopeapp.db.entity.TaskEntity
import com.omnixone.hopeapp.db.entity.TaskSessionEntity
import kotlinx.coroutines.flow.Flow

class TaskRepository(
    private val taskDao: TaskDao,
    private val sessionDao: TaskSessionDao
) {

    fun getAllTasks(): Flow<List<TaskEntity>> = taskDao.getAllTasks()


    fun insert(task: TaskEntity) {
        taskDao.insertTask(task)
    }

    suspend fun delete(task: TaskEntity) {
        taskDao.deleteTask(task)
    }

    suspend fun update(task: TaskEntity) {
        taskDao.updateTask(task)
    }

    suspend fun insertSession(session: TaskSessionEntity) {
        sessionDao.insertSession(session)
    }

    suspend fun updateSessionEndTime(taskUuid: String, endTime: Long, duration: Long) {
        sessionDao.updateEndTimeAndDuration(taskUuid, endTime, duration)
    }

    fun getSessionsByDate(date: String): List<TaskSessionEntity> {
        return sessionDao.getSessionsByDate(date)
    }

}
