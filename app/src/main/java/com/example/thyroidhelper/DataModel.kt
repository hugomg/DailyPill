package com.example.thyroidhelper

import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import java.util.*

typealias SharedPreferencesListener = SharedPreferences.OnSharedPreferenceChangeListener

object DataModel {

    const val IS_FIRST_DAY              = "is_first_day"
    const val DRUG_TAKEN_TIMESTAMP      = "drug_taken_timestamp"
    const val DAILY_REMINDER_ENABLED    = "daily_reminder_enabled"
    const val DAILY_REMINDER_TIME       = "daily_reminder_time"
    const val DAILY_REMINDER_LOCKSCREEN = "daily_reminder_lockscreen"

    private lateinit var sharedPrefs: SharedPreferences

    /**
     * This should be called on application startup, before anything else.
     */
    fun init(context: Context) {
        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context)
        PreferenceManager.setDefaultValues(context, R.xml.root_preferences, false)
    }

    //
    // IS_FIRST_DAY
    //

    fun isFirstDay(): Boolean {
        return sharedPrefs.getBoolean(IS_FIRST_DAY, true)
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
            .putBoolean(IS_FIRST_DAY, false)
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
    // DAILY_REMINDER_ENABLED
    //

    fun reminderIsEnabled(): Boolean {
        return sharedPrefs.getBoolean(DAILY_REMINDER_ENABLED, false)
    }

    //
    // DAILY_REMINDER_TIME
    //

    private fun getDailyReminderTime(): Pair<Int,Int> {
        val totalMinutes = sharedPrefs.getString(DAILY_REMINDER_TIME, null)!!
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
    // DAILY_REMINDER_LOCKSCREEN
    //

    fun displayReminderWhenLocked(): Boolean {
        return sharedPrefs.getBoolean(DAILY_REMINDER_LOCKSCREEN, true)
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