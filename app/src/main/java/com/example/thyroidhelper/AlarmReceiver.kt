package com.example.thyroidhelper

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import java.util.*

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        Notifications.sendMorningReminderNotification(true)
        Notifications.addAlarm(Calendar.getInstance(), true)
    }
}
