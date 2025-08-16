package com.omnixone.hopeapp.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.Worker
import androidx.work.WorkerParameters

import com.omnixone.hopeapp.MainActivity
import com.omnixone.hopeapp.R
import com.omnixone.hopeapp.broadcast_receiver.ChangeTaskReceiver
import com.omnixone.hopeapp.broadcast_receiver.DismissReceiver
import com.omnixone.hopeapp.service.NotificationMusicManager
import com.omnixone.hopeapp.service.NotificationMusicService
import org.json.JSONObject
import java.time.LocalTime
import java.util.concurrent.TimeUnit

class ReminderWorker(context: Context, workerParams: WorkerParameters) :
    Worker(context, workerParams) {

    companion object {
        private const val TAG = "ReminderWorker"
    }

    override fun doWork(): Result {
        Log.d(TAG, "doWork called")

        val now = LocalTime.now()
        Log.d(TAG, "Current time: $now")

        if (now.isBefore(LocalTime.of(9, 0)) || now.isAfter(LocalTime.of(23, 59))) {
            Log.d(TAG, "Time is outside allowed range (9AM-12AM). Rescheduling to 9 AM.")

            scheduleNextWork(30)
            return Result.success()
        }

        showNotification(applicationContext)

        Log.d(TAG, "Notification shown. Scheduling next one.")
        scheduleNextWork(30)
        return Result.success()
    }


    private fun showNotification(context: Context) {
        Log.d("SHIVANI", "showNotification: Function called")

        // Start the background music directly
        Log.d("SHIVANI", "showNotification: Starting music directly")
        NotificationMusicManager.startMusic(context)

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "task_reminder_channel"

        Log.d("SHIVANI", "showNotification: NotificationManager obtained")

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "Task Reminders", NotificationManager.IMPORTANCE_HIGH)
            notificationManager.createNotificationChannel(channel)
            Log.d("SHIVANI", "showNotification: Notification channel created for API >= O")
        } else {
            Log.d("SHIVANI", "showNotification: Running on API < O, no channel needed")
        }

        // Get random quote for notification text
        val randomQuote = getRandomQuote(context)

        // PendingIntent for "Same Task" action
        val dismissIntent = PendingIntent.getBroadcast(
            context, 0, Intent(context, DismissReceiver::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        Log.d("SHIVANI", "showNotification: DismissReceiver PendingIntent created")

        // PendingIntent for "Change Task" action
        val changeTaskIntent = PendingIntent.getBroadcast(
            context, 1, Intent(context, ChangeTaskReceiver::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        Log.d("SHIVANI", "showNotification: ChangeTaskReceiver PendingIntent created")

        val notification = NotificationCompat.Builder(context, channelId)
            .setContentTitle("Task Reminder")
            .setContentText(randomQuote)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .addAction(R.drawable.ic_task, "Same Task", dismissIntent)
            .addAction(R.drawable.ic_task, "Change Task", changeTaskIntent)
            .setAutoCancel(false) // Changed to false so music stops only on button press
            .setOngoing(true) // Prevents swipe to dismiss
            .build()

        Log.d("SHIVANI", "showNotification: Notification built successfully")

        try {
            notificationManager.notify(1001, notification)
            Log.d("SHIVANI", "showNotification: Notification displayed successfully with ID 1001")

            if (NotificationMusicManager.isPlaying()) {
                Log.d("SHIVANI", "showNotification: Confirmed - Music is playing!")
            } else {
                Log.e("SHIVANI", "showNotification: Warning - Music is not playing!")
            }
        } catch (e: Exception) {
            Log.e("SHIVANI", "showNotification: Error displaying notification", e)
        }
    }


    private fun scheduleNextWork(minutesLater: Long) {
        Log.d("SHIVANI", "Scheduling next work in $minutesLater minute(s)")

        val request = OneTimeWorkRequestBuilder<ReminderWorker>()
            .setInitialDelay(minutesLater, TimeUnit.MINUTES)
            .build()

        WorkManager.getInstance(applicationContext).enqueueUniqueWork(
            "TaskReminder", // Use same name
            ExistingWorkPolicy.REPLACE, // Always replace old scheduled one
            request
        )
    }



    // Helper function to get random quote
    private fun getRandomQuote(context: Context): String {
        return try {
            Log.d("SHIVANI", "getRandomQuote: Loading random quote for notification")

            // JSON file read करें
            val json = context.resources.openRawResource(R.raw.quotes).bufferedReader().use { it.readText() }

            // Parse करें
            val jsonObject = JSONObject(json)
            val quotesArray = jsonObject.getJSONArray("quotes")

            // Random quote pick करें
            val randomIndex = (0 until quotesArray.length()).random()
            val randomQuote = quotesArray.getString(randomIndex)

            Log.d("SHIVANI", "getRandomQuote: Selected quote at index $randomIndex")

            randomQuote

        } catch (e: Exception) {
            Log.e("SHIVANI", "getRandomQuote: Error loading quote", e)
            "Push yourself, because no one else is going to do it for you." // Fallback quote
        }
    }

}
