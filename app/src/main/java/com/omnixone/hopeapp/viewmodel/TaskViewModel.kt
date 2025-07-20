package com.omnixone.hopeapp.viewmodel
import android.app.Application
import androidx.lifecycle.*

import com.omnixone.hopeapp.db.entity.TaskEntity
import com.omnixone.hopeapp.db.entity.TaskSessionEntity
import com.omnixone.hopeapp.repository.TaskRepository
import com.omnixone.hopeapp.util.TimerPreference
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


class TaskViewModel(private val repository: TaskRepository,  application: Application) : ViewModel() {


    private val timerPref = TimerPreference(application.applicationContext)
    val allTasks: LiveData<List<TaskEntity>> = repository.getAllTasks().asLiveData()

    private var currentTimerJob: Job? = null
    private var _elapsedTime = MutableLiveData<Long>() // LiveData for timer
    val elapsedTime: LiveData<Long> = _elapsedTime

    private var currentTaskUuid: Int? = null
    private var startTime: Long = 0L
    private var baseElapsedTime: Long = 0L
    private val taskTodayDurations = mutableMapOf<Int, Long>()

    // Called when user clicks a task
    fun onTaskSelected(task: TaskEntity) {
        //Stop previous task timer if different
        if (currentTaskUuid != null && currentTaskUuid != task.uuid) {
            stopAndSaveSession()
        }

        //Start new timer if no task running
        if (currentTaskUuid != task.uuid) {
            startNewSession(task)
        }
    }

    fun getSelectedTaskUuid(): String? = currentTaskUuid.toString()
    fun getTodayDurationsMap(): Map<Int, Long> = taskTodayDurations


    // Start a new timer session
    private fun startNewSession(task: TaskEntity) {
        currentTaskUuid = task.uuid
        startTime = System.currentTimeMillis()
        //Get total time spent today from stored map
        baseElapsedTime = taskTodayDurations[task.uuid] ?: 0L
        // Save to SharedPreferences
        timerPref.saveRunningTask(task.uuid, startTime)

        // Insert session in DB with only start time
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

        viewModelScope.launch(Dispatchers.IO) {
            repository.insertSession(
                TaskSessionEntity(
                    uuid = 0, // auto-gen
                    taskUuid = task.uuid.toString(),
                    date = today,
                    startTime = startTime,
                    endTime = null,
                    duration = null
                )
            )
        }

        // Start timer UI
        startTimer()
    }

    // Stop and update previous session
    private fun stopAndSaveSession() {
        val endTime = System.currentTimeMillis()
        val duration = endTime - startTime

        viewModelScope.launch(Dispatchers.IO) {
            repository.updateSessionEndTime(currentTaskUuid.toString()!!, endTime, duration)

            // ✅ Reload today durations after session saved
            val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            val todaySessions = repository.getSessionsByDate(today)
            val grouped = todaySessions.groupBy { it.taskUuid.toInt() }

            taskTodayDurations.clear()
            for ((uuid, sessions) in grouped) {
                val total = sessions.sumOf { it.duration ?: 0L }
                taskTodayDurations[uuid] = total
            }

            // ✅ Update UI on main thread
            _elapsedTime.postValue(0L)
        }

        currentTimerJob?.cancel()
        currentTaskUuid = null
        startTime = 0L
        timerPref.clear()
    }


    // ⏱ Timer that ticks every second
    private fun startTimer() {
        currentTimerJob?.cancel()
        currentTimerJob = viewModelScope.launch {
            while (isActive) {
                val elapsed = System.currentTimeMillis() - startTime
                _elapsedTime.postValue(baseElapsedTime +elapsed)
                delay(1000)
            }
        }
    }

    // Call this on app resume to restore running timer
    fun resumeTimerIfRunning() {
        val uuid = timerPref.getRunningTaskUuid()
        val savedStartTime = timerPref.getStartTime()

        if (uuid != null && savedStartTime > 0L) {
            currentTaskUuid = uuid
            startTime = savedStartTime

            // ✅ Restore already worked time today from map
            baseElapsedTime = taskTodayDurations[uuid] ?: 0L

            startTimer()
        }
    }


    // Task operations
    fun delete(task: TaskEntity) = viewModelScope.launch(Dispatchers.IO) {
        repository.delete(task)
    }

    fun update(task: TaskEntity) = viewModelScope.launch (Dispatchers.IO) {
        repository.update(task)
    }

    fun insertTask(task: TaskEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.insert(task)
        }
    }


    fun loadTodayDurations(onLoaded: () -> Unit = {}) {
        viewModelScope.launch(Dispatchers.IO) {
            val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

            // 🔁 Get all today's sessions from DB
            val todaySessions = repository.getSessionsByDate(today) // You need to define this

            // 🧠 Group sessions by task UUID
            val grouped = todaySessions.groupBy { it.taskUuid.toInt() }

            taskTodayDurations.clear()

            for ((uuid, sessions) in grouped) {
                val total = sessions.sumOf { it.duration ?: 0L }
                taskTodayDurations[uuid] = total
            }

            //Notify adapter/UI (on main thread)
            onLoaded()
        }
    }


}

class TaskViewModelFactory(
    private val repository: TaskRepository,
    private val application: Application

) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TaskViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TaskViewModel(repository, application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}