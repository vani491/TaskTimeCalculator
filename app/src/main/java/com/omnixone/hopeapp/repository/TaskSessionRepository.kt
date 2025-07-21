package com.omnixone.hopeapp.repository

import com.omnixone.hopeapp.db.dao.TaskDao
import com.omnixone.hopeapp.db.dao.TaskSessionDao
import com.omnixone.hopeapp.db.entity.TaskEntity
import com.omnixone.hopeapp.db.entity.TaskSessionEntity
import kotlinx.coroutines.runBlocking

class TaskSessionRepository(private val taskSessionDao: TaskSessionDao,  private val taskDao: TaskDao) {


    fun getSessionsByDate(date: String): List<TaskSessionEntity> {
        return taskSessionDao.getSessionsByDate(date)
    }

    fun getAllTasks(): List<TaskEntity> {
        // You can later convert this to Flow for reactive updates
        return runBlocking {
            taskDao.getAllTask()
        }
    }
}
