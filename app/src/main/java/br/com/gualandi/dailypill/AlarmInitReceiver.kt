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

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import java.util.*

/**
 *  It is necessary to update the system alarms whenever the device reboots, or the system clock
 *  changes. I copied the list of intent-filters for this from the DeskClock app.
 *
 *  IMPORTANT NOTE: The MY_PACKAGE_REPLACED intent does not fire if you install the app through
 *  Android Studio's "run app" button.
 */
class AlarmInitReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        // TIME_CHANGED instead of TIME_SET is intentional.
        when (intent.action) {
            Intent.ACTION_BOOT_COMPLETED, Intent.ACTION_LOCALE_CHANGED,
            Intent.ACTION_MY_PACKAGE_REPLACED,
            Intent.ACTION_TIME_CHANGED, Intent.ACTION_TIMEZONE_CHANGED -> {
                if (DataModel.reminderIsEnabled()) {
                    val now = Calendar.getInstance()
                    Notifications.possiblyAddMissedNotification(now)
                    Notifications.addAlarm(now, false)
                } else {
                    Notifications.possiblyCancelTheNotification()
                    Notifications.removeAlarm()
                }
            }
        }
    }
}
