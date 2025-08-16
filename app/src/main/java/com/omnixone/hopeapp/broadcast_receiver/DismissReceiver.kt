package com.omnixone.hopeapp.broadcast_receiver

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.omnixone.hopeapp.service.NotificationMusicManager
import com.omnixone.hopeapp.service.NotificationMusicService


// 2. Update your DismissReceiver to stop music
class DismissReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        Log.d("SHIVANI", "DismissReceiver: onReceive called")

        // Stop the music
        NotificationMusicManager.stopMusic()

        // Cancel the notification
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(1001)
        Log.d("SHIVANI", "DismissReceiver: Notification cancelled")

        // Handle your existing dismiss logic here
        // ...
    }
}
