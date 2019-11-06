package br.com.gualandi.dailypill

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import java.util.*

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        Notifications.sendReminderNotification(true)
        Notifications.addAlarm(Calendar.getInstance(), true)
    }
}
