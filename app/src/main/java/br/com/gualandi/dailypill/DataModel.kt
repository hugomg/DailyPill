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

import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import java.util.*

typealias SharedPreferencesListener = SharedPreferences.OnSharedPreferenceChangeListener

@Suppress("MemberVisibilityCanBePrivate")
object DataModel {

    const val DRUG_TAKEN_TIMESTAMP = "drug_taken_timestamp"
    const val REMINDER_ENABLED     = "reminder_enabled"
    const val REMINDER_TIME        = "reminder_time"
    const val REMINDER_LOCK_SCREEN = "reminder_lock_screen"
    const val NEXT_ALARM_TIMESTAMP = "next_alarm_timestamp"

    private lateinit var sharedPrefs: SharedPreferences

    /**
     * This should be called on application startup, before anything else.
     */
    fun init(context: Context) {
        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context)
        PreferenceManager.setDefaultValues(context, R.xml.root_preferences, false)
    }

    //
    // DRUG_TAKEN_TIMESTAMP
    //

    fun getDrugTakenTimestamp(): Long {
        return sharedPrefs.getLong(DRUG_TAKEN_TIMESTAMP, 0)
    }

    fun takeDrugNow() {
        val timestamp = Calendar.getInstance().timeInMillis
        sharedPrefs.edit()
            .putLong(DRUG_TAKEN_TIMESTAMP, timestamp)
            .apply()
    }

    fun unsetDrugTakenTimestamp() {
        sharedPrefs.edit()
            .remove(DRUG_TAKEN_TIMESTAMP)
            .apply()
    }

    fun hasTakenDrugInTheSameDayAs(cal: Calendar): Boolean {
        val midnight = cal.clone() as Calendar
        midnight.set(Calendar.HOUR_OF_DAY, 0)
        midnight.set(Calendar.MINUTE, 0)
        midnight.set(Calendar.SECOND, 0)
        midnight.set(Calendar.MILLISECOND, 0)
        return midnight.timeInMillis <= getDrugTakenTimestamp()
    }

    fun hasTakenDrugToday(): Boolean {
        return hasTakenDrugInTheSameDayAs(Calendar.getInstance())
    }

    //
    // REMINDER_ENABLED
    //

    fun reminderIsEnabled(): Boolean {
        return sharedPrefs.getBoolean(REMINDER_ENABLED, false)
    }

    //
    // REMINDER_TIME
    //

    private fun getDailyReminderTime(): Pair<Int,Int> {
        val totalMinutes = sharedPrefs.getString(REMINDER_TIME, null)!!
        return parseTime(totalMinutes)
    }

    fun dailyReminderTimeForTheSameDayAs(now: Calendar): Calendar {
        val (hour, minute) = getDailyReminderTime()
        val reminderCal = now.clone() as Calendar
        reminderCal.set(Calendar.HOUR_OF_DAY, hour)
        reminderCal.set(Calendar.MINUTE,      minute)
        reminderCal.set(Calendar.SECOND, 0)
        reminderCal.set(Calendar.MILLISECOND, 0)
        return reminderCal
    }

    //
    // REMINDER_LOCK_SCREEN
    //

    fun displayReminderWhenLocked(): Boolean {
        return sharedPrefs.getBoolean(REMINDER_LOCK_SCREEN, true)
    }

    //
    // NEXT_ALARM_TIMESTAMP
    //

    fun getNextAlarmTimestamp(): Long {
        return sharedPrefs.getLong(NEXT_ALARM_TIMESTAMP, 0)
    }

    fun setNextAlarmTimestamp(timestamp: Long) {
        sharedPrefs.edit()
            .putLong(NEXT_ALARM_TIMESTAMP, timestamp)
            .apply()
    }

    //
    // Listeners
    //

    // We hold a strong reference to the listeners. Otherwise they might be GC-ed and ignored
    private val listeners: MutableSet<SharedPreferencesListener> = mutableSetOf()

    fun addListener(listener: SharedPreferencesListener) {
        listeners.add(listener)
        sharedPrefs.registerOnSharedPreferenceChangeListener(listener)
    }

    fun removeListener(listener: SharedPreferencesListener ) {
        listeners.remove(listener)
        sharedPrefs.unregisterOnSharedPreferenceChangeListener(listener)
    }
}