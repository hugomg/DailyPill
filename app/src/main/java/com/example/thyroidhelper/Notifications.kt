package com.example.thyroidhelper

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

private val MORNING_REMINDER_CHANNEL_ID = "channel_01"
private val MORNING_NOTIFICATION_ID = 1

fun createNotificationChannels(context: Context) {
    // Android O introduces mandatory notification channels.
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val channel01 = NotificationChannel(
            MORNING_REMINDER_CHANNEL_ID,
            "#Morning reminders",
            NotificationManager.IMPORTANCE_LOW)
        channel01.description =
            "#This reminder is sent in the middle of the night, while you are sleeping. " +
            "It opens the Daily Reminder dialog so that it is the first thing you see when you wake up."
        notificationManager.createNotificationChannel(channel01)
    }
}

fun sendMorningReminderNotification(context: Context) {

    val intent = Intent(context, ReminderActivity::class.java)
    intent.flags =
        Intent.FLAG_ACTIVITY_NEW_TASK or
        Intent.FLAG_ACTIVITY_CLEAR_TASK

    val pendingIntent =
        PendingIntent.getActivity(context, 0, intent, 0)

    val builder =
        NotificationCompat.Builder(context, MORNING_REMINDER_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("#My notification")
            .setContentText("#Hello World")
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setContentIntent(pendingIntent)

    val manager = NotificationManagerCompat.from(context)
    manager.notify(MORNING_NOTIFICATION_ID, builder.build())
}