package com.example.thyroidhelper

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import java.util.*

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

        if (key.equals(DataModel.MEDICATION_TIME)) {
            setAlarm()
        }
    }

    private fun updateNotifications() {
        val manager = NotificationManagerCompat.from(appContext)
        if (DataModel.hasTakenDrugToday()) {
            manager.cancel(MORNING_NOTIFICATION_ID)
        }
    }

    fun setAlarm() {
        val alarmHour   = DataModel.getMedicationTimeHours()
        val alarmMinute = DataModel.getMedicationTimeMinutes()

        val now = Calendar.getInstance()

        val timeToday = Calendar.getInstance()
        timeToday.set(Calendar.HOUR_OF_DAY, alarmHour)
        timeToday.set(Calendar.MINUTE,      alarmMinute)
        timeToday.set(Calendar.SECOND, 0)
        timeToday.set(Calendar.MILLISECOND, 0)

        val timeTomorrow = Calendar.getInstance()
        timeTomorrow.add(Calendar.DATE, 1)
        timeTomorrow.set(Calendar.HOUR_OF_DAY, alarmHour)
        timeTomorrow.set(Calendar.MINUTE,      alarmMinute)
        timeTomorrow.set(Calendar.SECOND, 0)
        timeTomorrow.set(Calendar.MILLISECOND, 0)

        //val nextTime = if (now.before(timeToday)) { timeToday } else { timeTomorrow }
        val nextTime =  timeToday

        val intent = Intent(appContext, AlarmReceiver::class.java)
        val pendingIntent =
            PendingIntent.getBroadcast(appContext, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)

        val alarmManager = appContext.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.setInexactRepeating(
            AlarmManager.RTC_WAKEUP,
            nextTime.timeInMillis,
            AlarmManager.INTERVAL_DAY,
            pendingIntent)

        val toastMsg = String.format("#Set alarm for %02d:%02d", alarmHour, alarmMinute)
        val toast = Toast.makeText(appContext, toastMsg, Toast.LENGTH_SHORT)
        toast.show()
    }
}