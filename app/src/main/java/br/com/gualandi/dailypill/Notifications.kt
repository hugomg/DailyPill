/*
 * Copyright © 2019 Hugo Musso Gualandi.
 * SPDX-License-Identifier: GPL-3.0-or-later
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package br.com.gualandi.dailypill

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import java.util.*

private const val REMINDER_CHANNEL_ID = "channel_01"
private const val NOTIFICATION_ID = 1

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
            REMINDER_CHANNEL_ID,
            appContext.getString(R.string.reminder),
            NotificationManager.IMPORTANCE_HIGH)
        channel01.description = appContext.getString(R.string.notification_channel_description_1)

        notificationManager.createNotificationChannel(channel01)
    }

    fun sendReminderNotification(requestFullScreen: Boolean) {
        val now = Calendar.getInstance()
        if (DataModel.hasTakenDrugInTheSameDayAs(now)) { return }

        createNotificationChannels()

        val intent = Intent(appContext, ReminderActivity::class.java)
        intent.flags =
            Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TASK

        val pendingIntent =
            PendingIntent.getActivity(appContext, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)

        val builder =
            NotificationCompat.Builder(appContext, REMINDER_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_pill)
                .setContentTitle(appContext.getString(R.string.notification_title))
                .setContentText(appContext.getString(R.string.notification_description))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_REMINDER)
                .setContentIntent(pendingIntent)
                .setTimeoutAfter(14*60*1000) // 14h later
                .setWhen(DataModel.dailyReminderTimeForTheSameDayAs(now).timeInMillis)
                .setOnlyAlertOnce(true)
        if (requestFullScreen && DataModel.displayReminderWhenLocked()) {
            builder.setFullScreenIntent(pendingIntent, true)
        }

        val manager = NotificationManagerCompat.from(appContext)
        manager.notify(NOTIFICATION_ID, builder.build())
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        if (key.equals(DataModel.DRUG_TAKEN_TIMESTAMP)
            || key.equals(DataModel.REMINDER_ENABLED)
            || key.equals(DataModel.REMINDER_TIME)
        ) {
            Thread(Runnable {
                possiblyCancelTheNotification()
                if (DataModel.reminderIsEnabled()) {
                    addAlarm(Calendar.getInstance(), false)
                } else {
                    removeAlarm()
                }
            }).run()
        }
    }

    fun possiblyCancelTheNotification() {
        val now = Calendar.getInstance()
        val reminderEnabled= DataModel.reminderIsEnabled()
        val hasMedicated= DataModel.hasTakenDrugInTheSameDayAs(now)
        val reminderTime= DataModel.dailyReminderTimeForTheSameDayAs(now)
        if (!reminderEnabled || hasMedicated || now.before(reminderTime)) {
            NotificationManagerCompat.from(appContext).cancel(NOTIFICATION_ID)
        }
    }

    // Do we have missed notifications?
    fun possiblyAddMissedNotification(now: Calendar) {
        val timeToday = DataModel.dailyReminderTimeForTheSameDayAs(now)
        if (now.after(timeToday) && !DataModel.hasTakenDrugInTheSameDayAs(now)) {
            sendReminderNotification(false)
        }
    }

    private fun alarmPendingIntent(): PendingIntent {
        val intent = Intent(appContext, AlarmReceiver::class.java)
        return PendingIntent.getBroadcast(
            appContext,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT)
    }

    fun addAlarm(now: Calendar, onlyTomorrow: Boolean) {
        val timeToday = DataModel.dailyReminderTimeForTheSameDayAs(now)

        val timeTomorrow = timeToday.clone() as Calendar
        timeTomorrow.add(Calendar.DATE, 1)

        val nowIsAfterTodaysMedicine = now.after(timeToday)
        val alarmTime =
            if (onlyTomorrow || nowIsAfterTodaysMedicine) { timeTomorrow } else { timeToday }

        val pendingIntent = alarmPendingIntent()

        // Starting with API level 19, alarm delivery on android is inexact, and there is a delivery
        // window that the system takes advantage of to optimize battery usage. Unfortunately, the
        // default delivery window for alarms being set to a day from now can be of 18h or more,
        // which is prohibitive. This precludes us from using the setRepeating family of functions.
        // The only option is to use `setWindow`, or one of the variations of `setExact`.
        // By the way, `adb shell dumpsys alarm` can be used to debug the state of the alarm system.
        val alarmManager =
            appContext.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        if (Build.VERSION.SDK_INT >= 19) {
            // A delivery window of 1 minute ensures that the alarm arrives when the user expects
            val windowLengthInMillis = 1*60*1000L
            alarmManager.setWindow(
                AlarmManager.RTC_WAKEUP,
                alarmTime.timeInMillis,
                windowLengthInMillis,
                pendingIntent)
        } else {
            alarmManager.set(
                AlarmManager.RTC_WAKEUP,
                alarmTime.timeInMillis,
                pendingIntent)
        }

        // val toastMsg = String.format("#Set alarm for %s, %02d:%02d",
        //     (if (nowIsAfterTodaysMedicine) {"tomorrow"} else {"today"}),
        //     alarmTime.get(Calendar.HOUR_OF_DAY),
        //     alarmTime.get(Calendar.MINUTE))
        // val toast = Toast.makeText(appContext, toastMsg, Toast.LENGTH_SHORT)
        // toast.show()
    }

    fun removeAlarm() {
        val alarmManager = appContext.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.cancel(alarmPendingIntent())
        // Toast.makeText(appContext, "#Alarm removed", Toast.LENGTH_SHORT).show()
    }
}