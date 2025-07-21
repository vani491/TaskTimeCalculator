package com.omnixone.hopeapp.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.omnixone.hopeapp.repository.TaskSessionRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

class DashboardViewModel (
    private val repository: TaskSessionRepository
) : ViewModel() {

    val totalTime = MutableLiveData<String>()
    val totalSessions = MutableLiveData<Int>()
    val totalTasks = MutableLiveData<Int>()
    val topTask = MutableLiveData<String>()

    val barChartData = MutableLiveData<List<Pair<String, Float>>>()
    val pieChartData = MutableLiveData<List<Pair<String, Float>>>()

    fun loadDashboard(date: String) {
        viewModelScope.launch {
            // Fetch session and task data in parallel
            val sessionsDeferred = async(Dispatchers.IO) { repository.getSessionsByDate(date) }
            val tasksDeferred = async(Dispatchers.IO) { repository.getAllTasks() }

            val sessions = sessionsDeferred.await()
            val tasks = tasksDeferred.await()

            val taskMap = tasks.associateBy { it.uuid.toString() }

            // 1️⃣ Total Time
            val totalMillis = sessions.mapNotNull { it.duration }.sum()
            totalTime.postValue(formatMillisToHHmm(totalMillis))

            // 2️⃣ Total Sessions
            totalSessions.postValue(sessions.size)

            // 3️⃣ Total Unique Tasks
            totalTasks.postValue(sessions.map { it.taskUuid }.distinct().size)

            // 4️⃣ Top Task
            val topTaskUuid = sessions
                .groupBy { it.taskUuid }
                .mapValues { it.value.sumOf { session -> session.duration ?: 0L } }
                .maxByOrNull { it.value }?.key

            topTask.postValue(taskMap[topTaskUuid]?.title ?: "None")

            // 5️⃣ Bar Chart: Task Title vs Duration (hours)
            val barData = sessions
                .groupBy { it.taskUuid }
                .map { (uuid, list) ->
                    val hours = list.sumOf { it.duration ?: 0L } / 3600000f
                    val title = taskMap[uuid]?.title ?: "Unknown"
                    title to hours
                }
            barChartData.postValue(barData)

            // 6️⃣ Pie Chart: Task Title vs Percentage
            val totalForPie = barData.sumOf { it.second.toDouble() }.toFloat().takeIf { it > 0 } ?: 1f
            val pieData = barData.map { (title, hours) ->
                title to (hours / totalForPie * 100f)
            }
            pieChartData.postValue(pieData)
        }
    }

    private fun formatMillisToHHmm(millis: Long): String {
        val hours = TimeUnit.MILLISECONDS.toHours(millis)
        val minutes = TimeUnit.MILLISECONDS.toMinutes(millis) % 60
        return String.format("%02d:%02d", hours, minutes)
    }
}
