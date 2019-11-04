package com.example.thyroidhelper

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import java.lang.IllegalArgumentException
import java.util.*

/**
 *  It is necessary to update the system alarms whenever the device reboots, or the system clock
 *  changes. I copied the list of intent-filters for this from the DeskClock app.
 */
class AlarmInitReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        // TIME_CHANGED instead of TIME_SET is intentional.
        when (intent.action) {
            Intent.ACTION_BOOT_COMPLETED, Intent.ACTION_LOCALE_CHANGED,
            Intent.ACTION_MY_PACKAGE_REPLACED,
            Intent.ACTION_TIME_CHANGED, Intent.ACTION_TIMEZONE_CHANGED -> {
                val now = Calendar.getInstance()
                Notifications.possiblyAddMissedNotification(now)
                Notifications.addAlarm(now, false)
            }
            else-> {
                throw IllegalArgumentException("Unexpected action " + intent.action)
            }
        }
    }
}
