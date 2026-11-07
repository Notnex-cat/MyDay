package com.notnex.myday.notification

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.notnex.myday.R
import com.notnex.myday.ui.MainActivity
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class ReminderNotifier@Inject constructor(@ApplicationContext private val context: Context) {
    fun createNotificationChannel(){
        // Create the NotificationChannel.
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val mChannel = NotificationChannel("notification_channel_id", context.getString(R.string.notification_channel_name), importance)
        mChannel.description = "descriptionText"
        // Register the channel with the system. You can't change the importance
        // or other notification behaviors after this.
        val notificationManager =
            context.getSystemService(NotificationManager::class.java)
        notificationManager?.createNotificationChannel(mChannel)
    }


    fun show() {
        createNotificationChannel()

        val intent = Intent(context, MainActivity::class.java)

        val pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE)

        val notification = NotificationCompat.Builder(context, "notification_channel_id")
            .setSmallIcon(R.drawable.ic_launcher_monochrome)
            .setContentTitle("Заголовок")
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setContentText("Текст")
            .build()

        val granted = ContextCompat.checkSelfPermission(
            context, Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED

        if (granted) {
            NotificationManagerCompat.from(context).notify(1, notification)
        }


    }
}