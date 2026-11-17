package com.focusx.app.timer

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.focusx.app.R

class FocusTimerService : Service() {
    companion object {
        const val ACTION_START = "com.focusx.app.timer.START"
        const val ACTION_STOP = "com.focusx.app.timer.STOP"
        const val EXTRA_END_AT = "end_at"
        private const val CHANNEL = "focus_timer"
        private const val NOTIFICATION_ID = 410
    }

    override fun onCreate() {
        super.onCreate()
        if (Build.VERSION.SDK_INT >= 26) {
            val channel = NotificationChannel(CHANNEL, "Focus timer", NotificationManager.IMPORTANCE_LOW)
            getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_STOP -> {
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
                return START_NOT_STICKY
            }
            ACTION_START -> {
                val endAt = intent.getLongExtra(EXTRA_END_AT, System.currentTimeMillis() + 25 * 60_000L)
                startForeground(NOTIFICATION_ID, notification(endAt))
            }
        }
        return START_STICKY
    }

    private fun notification(endAt: Long): Notification {
        val remaining = ((endAt - System.currentTimeMillis()).coerceAtLeast(0) / 60_000L).toInt()
        return NotificationCompat.Builder(this, CHANNEL)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle("FocusX session running")
            .setContentText("$remaining minutes remaining — stay focused")
            .setOngoing(true)
            .setCategory(NotificationCompat.CATEGORY_PROGRESS)
            .build()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
