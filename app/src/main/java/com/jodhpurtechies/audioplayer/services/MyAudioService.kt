package com.jodhpurtechies.audioplayer.services

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import com.jodhpurtechies.audioplayer.ui.audioPlayer.AudioPlayerActivity

class MyAudioService : Service() {
    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        when (val notificationId = intent?.extras?.getInt("NotificationId") ?: 0) {
            0, 1 -> {
                Log.d("NotificationId", "" + notificationId)
                stopForeground(notificationId == 0)
                if (notificationId == 0)
                    stopSelf()
            }
            else -> startForeground(notificationId, AudioPlayerActivity.activeNotification)
        }

        return super.onStartCommand(intent, flags, startId)
    }


}