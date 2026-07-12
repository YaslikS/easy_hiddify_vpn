package com.yasliks.hiddify_library_lib.utils

//noinspection SuspiciousImport
import android.R
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import com.hiddify.core.libbox.Notification

class HiddifyNotificationUtils(
    private val context: Context,
) {

    companion object {
        const val CHANNEL_ID = "hiddify_channel"
        const val NOTIFICATION_ID = 1001
    }

    fun createNotification(
        serverName: String?,
        notification: Notification? = null
    ): android.app.Notification {
        val manager = context.getSystemService(
            /* name = */ Context.NOTIFICATION_SERVICE,
        ) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                /* id = */ CHANNEL_ID,
                /* name = */ "Hiddify VPN Status",
                /* importance = */ NotificationManager.IMPORTANCE_LOW,
            )
            manager.createNotificationChannel(channel)
        }

        val title = notification?.title ?: serverName ?: "Hiddify VPN"
        val content = notification?.body ?: "Connected"

        return NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(content)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .build()
    }
}