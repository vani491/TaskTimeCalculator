package com.omnixone.hopeapp.broadcast_receiver

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.omnixone.hopeapp.MainActivity
import com.omnixone.hopeapp.service.NotificationMusicManager
import com.omnixone.hopeapp.service.NotificationMusicService

class ChangeTaskReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        Log.d("SHIVANI", "ChangeTaskReceiver: onReceive called")

        // Stop the music
        NotificationMusicManager.stopMusic()

        // Cancel the notification
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(1001)
        Log.d("SHIVANI", "ChangeTaskReceiver: Notification cancelled")

        // Open the main activity
        val openAppIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        Log.d("SHIVANI", "ChangeTaskReceiver: Starting MainActivity")
        context.startActivity(openAppIntent)
    }
}