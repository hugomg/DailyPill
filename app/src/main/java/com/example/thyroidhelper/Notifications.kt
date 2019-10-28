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

fun createNotificationChannels(ctx: Context) {
    // Android O introduces mandatory notification channels.
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

        val notificationManager =
            ctx.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val channel01 = NotificationChannel(
            MORNING_REMINDER_CHANNEL_ID,
            ctx.getString(R.string.notification_channel_name_1),
            NotificationManager.IMPORTANCE_HIGH)
        channel01.description = ctx.getString(R.string.notification_channel_description_1)
        notificationManager.createNotificationChannel(channel01)
    }
}

fun sendMorningReminderNotification(ctx: Context) {

    val intent = Intent(ctx, ReminderActivity::class.java)
    intent.flags =
        Intent.FLAG_ACTIVITY_NEW_TASK or
        Intent.FLAG_ACTIVITY_CLEAR_TASK

    val pendingIntent =
        PendingIntent.getActivity(ctx, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)

    val builder =
        NotificationCompat.Builder(ctx, MORNING_REMINDER_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(ctx.getString(R.string.notification_morning_title))
            .setContentText(ctx.getString(R.string.notification_morning_description))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setFullScreenIntent(pendingIntent, true)

    val manager = NotificationManagerCompat.from(ctx)
    manager.notify(MORNING_NOTIFICATION_ID, builder.build())
}

fun updateNotifications(ctx: Context) {
    val manager = NotificationManagerCompat.from(ctx)

    if (hasTakenDrugToday(ctx)) {
        manager.cancel(MORNING_NOTIFICATION_ID)
    } else {
        // TODO
    }
}