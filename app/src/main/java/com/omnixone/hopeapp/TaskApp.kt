package com.omnixone.hopeapp


import android.app.Application
import com.omnixone.hopeapp.db.AppDatabase


class TaskApp : Application() {

    lateinit var database: AppDatabase
        private set

    override fun onCreate() {
        super.onCreate()
        database = AppDatabase.getDatabase(this)
    }
}
