package com.omnixone.hopeapp.service

import android.content.Context
import android.media.MediaPlayer
import android.util.Log

object NotificationMusicManager {
    private var mediaPlayer: MediaPlayer? = null

    fun startMusic(context: Context) {
        Log.d("SHIVANI", "NotificationMusicManager: startMusic() called")

        if (mediaPlayer == null) {
            Log.d("SHIVANI", "NotificationMusicManager: MediaPlayer is null, creating new instance")
            try {
                val resourceId = context.resources.getIdentifier("notification_music", "raw", context.packageName)
                Log.d("SHIVANI", "NotificationMusicManager: Resource ID for notification_music: $resourceId")

                if (resourceId == 0) {
                    Log.e("SHIVANI", "NotificationMusicManager: Raw resource 'notification_music' not found!")
                    return
                }

                mediaPlayer = MediaPlayer.create(context, resourceId)

                if (mediaPlayer == null) {
                    Log.e("SHIVANI", "NotificationMusicManager: MediaPlayer.create() returned null")
                    return
                }

                Log.d("SHIVANI", "NotificationMusicManager: MediaPlayer created successfully")
                mediaPlayer?.isLooping = true
                Log.d("SHIVANI", "NotificationMusicManager: Set looping to true")

                mediaPlayer?.start()
                Log.d("SHIVANI", "NotificationMusicManager: MediaPlayer.start() called")

                if (mediaPlayer?.isPlaying == true) {
                    Log.d("SHIVANI", "NotificationMusicManager: Music is now playing!")
                } else {
                    Log.e("SHIVANI", "NotificationMusicManager: Music failed to start playing")
                }

            } catch (e: Exception) {
                Log.e("SHIVANI", "NotificationMusicManager: Error starting music", e)
            }
        } else {
            Log.d("SHIVANI", "NotificationMusicManager: MediaPlayer already exists")
            if (mediaPlayer?.isPlaying == true) {
                Log.d("SHIVANI", "NotificationMusicManager: Music is already playing")
            } else {
                Log.d("SHIVANI", "NotificationMusicManager: Starting existing MediaPlayer")
                mediaPlayer?.start()
            }
        }
    }

    fun stopMusic() {
        Log.d("SHIVANI", "NotificationMusicManager: stopMusic() called")

        mediaPlayer?.let {
            Log.d("SHIVANI", "NotificationMusicManager: MediaPlayer exists")
            if (it.isPlaying) {
                Log.d("SHIVANI", "NotificationMusicManager: Music is playing, stopping...")
                it.stop()
                Log.d("SHIVANI", "NotificationMusicManager: Music stopped")
            } else {
                Log.d("SHIVANI", "NotificationMusicManager: Music was not playing")
            }
            it.release()
            Log.d("SHIVANI", "NotificationMusicManager: MediaPlayer released")
            mediaPlayer = null
        } ?: Log.d("SHIVANI", "NotificationMusicManager: MediaPlayer was null")
    }

    fun isPlaying(): Boolean {
        return mediaPlayer?.isPlaying ?: false
    }
}
