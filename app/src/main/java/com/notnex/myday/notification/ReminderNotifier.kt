package com.notnex.myday.notification

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.annotation.StringRes
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import com.notnex.myday.R
import com.notnex.myday.ui.MainActivity
import com.notnex.myday.ui.navigation.DAY_DEEP_LINK
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.LocalDate
import javax.inject.Inject

class ReminderNotifier@Inject constructor(@ApplicationContext private val context: Context) {
    fun createNotificationChannel(){
        // Create the NotificationChannel.
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val mChannel = NotificationChannel("notification_channel_id", context.getString(R.string.notification_channel_name), importance)
        mChannel.description = context.getString(R.string.notification_channel_description)
        // Register the channel with the system. You can't change the importance
        // or other notification behaviors after this.
        val notificationManager =
            context.getSystemService(NotificationManager::class.java)
        notificationManager?.createNotificationChannel(mChannel)
    }


    fun show(@StringRes message: Int) {
        createNotificationChannel()

        val intent = Intent(Intent.ACTION_VIEW, "$DAY_DEEP_LINK/${LocalDate.now()}".toUri(), context, MainActivity::class.java)

        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        val pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE)

        val notification = NotificationCompat.Builder(context, "notification_channel_id")
            .setSmallIcon(R.drawable.ic_launcher_monochrome)
            .setContentTitle(context.getString(R.string.how_day_gone))
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setContentText(context.getString(message))
            .build()

        val granted = ContextCompat.checkSelfPermission(
            context, Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED

        if (granted) {
            NotificationManagerCompat.from(context).notify(1, notification)
        }


    }
}