package com.example.thyroidhelper

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/**
 *  It is necessary to update the system alarms whenever the device reboots, or the system clock
 *  changes. I copied the list of intent-filters for this from the DeskClock app.
 */
class AlarmInitReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        Notifications.setAlarm()
    }
}
