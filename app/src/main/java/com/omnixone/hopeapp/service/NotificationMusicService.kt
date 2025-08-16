package com.omnixone.hopeapp.service

import android.app.Service
import android.content.Intent
import android.media.MediaPlayer
import android.os.Binder
import android.os.IBinder
import android.util.Log
import com.omnixone.hopeapp.R

class NotificationMusicService : Service() {
    private var mediaPlayer: MediaPlayer? = null
    private val binder = MusicBinder()

    inner class MusicBinder : Binder() {
        fun getService(): NotificationMusicService = this@NotificationMusicService
    }

    override fun onBind(intent: Intent): IBinder {
        Log.d("SHIVANI", "NotificationMusicService: onBind called")
        return binder
    }

    override fun onCreate() {
        super.onCreate()
        Log.d("SHIVANI", "NotificationMusicService: onCreate called")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d("SHIVANI", "NotificationMusicService: onStartCommand called with action: ${intent?.action}")

        when (intent?.action) {
            ACTION_START_MUSIC -> {
                Log.d("SHIVANI", "NotificationMusicService: ACTION_START_MUSIC received")
                startMusic()
            }
            ACTION_STOP_MUSIC -> {
                Log.d("SHIVANI", "NotificationMusicService: ACTION_STOP_MUSIC received")
                stopMusic()
            }
            else -> {
                Log.d("SHIVANI", "NotificationMusicService: Unknown action or null intent")
            }
        }
        return START_NOT_STICKY
    }

    private fun startMusic() {
        Log.d("SHIVANI", "NotificationMusicService: startMusic() called")

        if (mediaPlayer == null) {
            Log.d("SHIVANI", "NotificationMusicService: MediaPlayer is null, creating new instance")
            try {
                // Check if raw resource exists - यहाँ अपनी MP3 file का नाम डालें (बिना .mp3 के)
                val resourceId = resources.getIdentifier("notification_music", "raw", packageName)
                // अगर आपकी file का नाम अलग है तो "notification_music" की जगह वो नाम लिखें
                Log.d("SHIVANI", "NotificationMusicService: Resource ID for notification_music: $resourceId")

                if (resourceId == 0) {
                    Log.e("SHIVANI", "NotificationMusicService: Raw resource 'notification_music' not found!")
                    return
                }

                mediaPlayer = MediaPlayer.create(this, resourceId)

                if (mediaPlayer == null) {
                    Log.e("SHIVANI", "NotificationMusicService: MediaPlayer.create() returned null")
                    return
                }

                Log.d("SHIVANI", "NotificationMusicService: MediaPlayer created successfully")
                mediaPlayer?.isLooping = true
                Log.d("SHIVANI", "NotificationMusicService: Set looping to true")

                mediaPlayer?.start()
                Log.d("SHIVANI", "NotificationMusicService: MediaPlayer.start() called")

                if (mediaPlayer?.isPlaying == true) {
                    Log.d("SHIVANI", "NotificationMusicService: Music is now playing!")
                } else {
                    Log.e("SHIVANI", "NotificationMusicService: Music failed to start playing")
                }

            } catch (e: Exception) {
                Log.e("SHIVANI", "NotificationMusicService: Error starting music", e)
            }
        } else {
            Log.d("SHIVANI", "NotificationMusicService: MediaPlayer already exists, checking if playing")
            if (mediaPlayer?.isPlaying == true) {
                Log.d("SHIVANI", "NotificationMusicService: Music is already playing")
            } else {
                Log.d("SHIVANI", "NotificationMusicService: MediaPlayer exists but not playing, starting...")
                mediaPlayer?.start()
            }
        }
    }

    private fun stopMusic() {
        Log.d("SHIVANI", "NotificationMusicService: stopMusic() called")

        mediaPlayer?.let {
            Log.d("SHIVANI", "NotificationMusicService: MediaPlayer exists, checking if playing")
            if (it.isPlaying) {
                Log.d("SHIVANI", "NotificationMusicService: Music is playing, stopping...")
                it.stop()
                Log.d("SHIVANI", "NotificationMusicService: Music stopped")
            } else {
                Log.d("SHIVANI", "NotificationMusicService: Music was not playing")
            }
            it.release()
            Log.d("SHIVANI", "NotificationMusicService: MediaPlayer released")
            mediaPlayer = null
        } ?: Log.d("SHIVANI", "NotificationMusicService: MediaPlayer was null")

        stopSelf()
        Log.d("SHIVANI", "NotificationMusicService: Service stopped")
    }

    override fun onDestroy() {
        Log.d("SHIVANI", "NotificationMusicService: onDestroy called")
        stopMusic()
        super.onDestroy()
    }

    companion object {
        const val ACTION_START_MUSIC = "START_MUSIC"
        const val ACTION_STOP_MUSIC = "STOP_MUSIC"
    }
}





