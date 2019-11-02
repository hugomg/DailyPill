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
                .setSmallIcon(R.drawable.ic_pill)
                .setContentTitle(appContext.getString(R.string.notification_morning_title))
                .setContentText(appContext.getString(R.string.notification_morning_description))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_REMINDER)
                .setFullScreenIntent(pendingIntent, true)
                .setOngoing(true)
                .setTimeoutAfter(14*60*1000) // 14h later

        val manager = NotificationManagerCompat.from(appContext)
        manager.notify(MORNING_NOTIFICATION_ID, builder.build())
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        if (key.equals(DataModel.DRUG_TAKEN_TIMESTAMP) || key.equals(DataModel.MEDICATION_TIME)) {
            updateNotifications()
        }

        if (key.equals(DataModel.MEDICATION_TIME)) {
            setAlarm()
        }
    }

    private fun updateNotifications() {
        val now = Calendar.getInstance()
        val hasMedicated = DataModel.hasTakenDrugInTheSameDayAs(now)
        val medicineTime = DataModel.medicationTimeForTheSameDayAs(now)
        if (hasMedicated || now.before(medicineTime)) {
            NotificationManagerCompat.from(appContext).cancel(MORNING_NOTIFICATION_ID)
        }
    }

    fun setAlarm() {
        val now = Calendar.getInstance()
        val timeToday = DataModel.medicationTimeForTheSameDayAs(now)

        val timeTomorrow = timeToday.clone() as Calendar
        timeTomorrow.add(Calendar.DATE, 1)

        val nowIsAfterTodaysMedicine = now.after(timeToday)
        val alarmTime = if (nowIsAfterTodaysMedicine) { timeTomorrow } else { timeToday }

        // Do we have missed notifications? If we just installed the app, assume that the user has
        // already taken their medicine earlier today.
        val hasTakenMedicine =
            DataModel.isFirstDay() || DataModel.hasTakenDrugInTheSameDayAs(now)
        if (nowIsAfterTodaysMedicine && !hasTakenMedicine) {
            sendMorningReminderNotification()
        }

        val intent = Intent(appContext, AlarmReceiver::class.java)
        val pendingIntent =
            PendingIntent.getBroadcast(appContext, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)

        val alarmManager = appContext.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.setInexactRepeating(
            AlarmManager.RTC_WAKEUP,
            alarmTime.timeInMillis,
            AlarmManager.INTERVAL_DAY,
            pendingIntent)

        val toastMsg = String.format("#Set alarm for %s, %02d:%02d",
            (if (nowIsAfterTodaysMedicine) {"tomorrow"} else {"today"}),
            alarmTime.get(Calendar.HOUR_OF_DAY),
            alarmTime.get(Calendar.MINUTE))
        val toast = Toast.makeText(appContext, toastMsg, Toast.LENGTH_SHORT)
        toast.show()
    }
}