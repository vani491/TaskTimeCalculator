package com.omnixone.hopeapp.util

import android.content.Context
import android.content.SharedPreferences

class TimerPreference(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("TimerPrefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_TASK_UUID = "task_uuid"
        private const val KEY_START_TIME = "start_time"
    }

    fun saveRunningTask(uuid: Int, startTime: Long) {
        prefs.edit().apply {
            putInt(KEY_TASK_UUID, uuid)
            putLong(KEY_START_TIME, startTime)
            apply()
        }
    }

    fun getRunningTaskUuid(): Int = prefs.getInt(KEY_TASK_UUID, 0)

    fun getStartTime(): Long = prefs.getLong(KEY_START_TIME, 0L)

    fun clear() {
        prefs.edit().clear().apply()
    }
}
