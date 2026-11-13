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

/** Показывает уведомление напоминания; что и когда показывать, решает ReminderReceiver. */
class ReminderNotifier@Inject constructor(@ApplicationContext private val context: Context) {
    fun createNotificationChannel(){
        // Вызывается перед каждым показом: повторное создание безопасно и обновляет имя
        // и описание канала. Важность после первого создания система уже не меняет.
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val mChannel = NotificationChannel("notification_channel_id", context.getString(R.string.notification_channel_name), importance)
        mChannel.description = context.getString(R.string.notification_channel_description)
        val notificationManager =
            context.getSystemService(NotificationManager::class.java)
        notificationManager?.createNotificationChannel(mChannel)
    }


    fun show(@StringRes message: Int, type: ReminderType) {
        createNotificationChannel()

        // Нажатие открывает заметку дня по deep link. Дата — того дня, о котором напоминание:
        // уведомление в 23:50, открытое после полуночи, должно вести во вчерашний день.
        val intent = Intent(Intent.ACTION_VIEW, "$DAY_DEEP_LINK/${LocalDate.now()}".toUri(), context, MainActivity::class.java)

        // С этими флагами NavController строит стек «главный → заметка»: «Назад» ведёт
        // на главный экран, а вторая копия MainActivity поверх первой не появляется.
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        val pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE)

        val notification = NotificationCompat.Builder(context, "notification_channel_id")
            .setSmallIcon(R.drawable.ic_launcher_monochrome)
            .setContentTitle(context.getString(type.title))
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setContentText(context.getString(message))
            .build()

        val granted = ContextCompat.checkSelfPermission(
            context, Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED

        // Номер уведомления — тип: новое напоминание того же типа заменяет вчерашнее,
        // а разные типы друг друга не затирают.
        if (granted) {
            NotificationManagerCompat.from(context).notify(type.requestCode, notification)
        }
    }
}