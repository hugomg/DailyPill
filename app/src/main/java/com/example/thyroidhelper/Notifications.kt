package com.example.thyroidhelper

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

private const val MORNING_REMINDER_CHANNEL_ID = "channel_01"
private const val MORNING_NOTIFICATION_ID = 1

object Notifications: SharedPreferencesListener {

    private lateinit var appContext: Context

    fun init(context: Context) {
        appContext = context.applicationContext
        DataModel.addListener(this)
    }

    /**
     * Register this app's notification channels with the system.
     * They were introduced and made mandatory in Android O.
     */
    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) { return }

        val notificationManager =
            appContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val channel01 = NotificationChannel(
            MORNING_REMINDER_CHANNEL_ID,
            appContext.getString(R.string.notification_channel_name_1),
            NotificationManager.IMPORTANCE_HIGH)
        channel01.description = appContext.getString(R.string.notification_channel_description_1)

        notificationManager.createNotificationChannel(channel01)
    }

    fun sendMorningReminderNotification() {
        if (DataModel.hasTakenDrugToday()) { return }

        createNotificationChannels()

        val intent = Intent(appContext, ReminderActivity::class.java)
        intent.flags =
            Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TASK

        val pendingIntent =
            PendingIntent.getActivity(appContext, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)

        val builder =
            NotificationCompat.Builder(appContext, MORNING_REMINDER_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle(appContext.getString(R.string.notification_morning_title))
                .setContentText(appContext.getString(R.string.notification_morning_description))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_REMINDER)
                .setFullScreenIntent(pendingIntent, true)

        val manager = NotificationManagerCompat.from(appContext)
        manager.notify(MORNING_NOTIFICATION_ID, builder.build())
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        if (key.equals(DataModel.DRUG_TAKEN_TIMESTAMP)) {
            updateNotifications()
        }
    }

    private fun updateNotifications() {
        val manager = NotificationManagerCompat.from(appContext)
        if (DataModel.hasTakenDrugToday()) {
            manager.cancel(MORNING_NOTIFICATION_ID)
        } else {
            // TODO
        }
    }

}