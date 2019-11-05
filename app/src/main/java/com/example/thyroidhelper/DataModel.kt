package com.example.thyroidhelper

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.preference.PreferenceManager
import java.util.*

typealias SharedPreferencesListener = SharedPreferences.OnSharedPreferenceChangeListener

object DataModel {

    const val IS_FIRST_DAY                = "is_first_day"
    const val DRUG_TAKEN_TIMESTAMP        = "drug_taken_timestamp"
    const val MORNING_REMINDER_ENABLED    = "morning_reminder_enabled"
    const val MORNING_REMINDER_TIME       = "morning_reminder_time"
    const val MORNING_REMINDER_LOCKSCREEN = "morning_reminder_lockscreen"

    private lateinit var sharedPrefs: SharedPreferences

    /**
     * This should be called on application startup, before anything else.
     */
    fun init(context: Context) {
        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context)
        PreferenceManager.setDefaultValues(context, R.xml.root_preferences, false)
        Log.d("DEFAULT", "setting defaults")
        Log.d("DEFAULT", sharedPrefs.getBoolean(MORNING_REMINDER_ENABLED, true).toString())
        Log.d("DEFAULT", sharedPrefs.getString(MORNING_REMINDER_TIME, "XXX"))
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
    // MORNING_REMINDER_ENABLED
    //

    fun reminderIsEnabled(): Boolean {
        return sharedPrefs.getBoolean(MORNING_REMINDER_ENABLED, false)
    }

    //
    // MORNING_REMINDER_TIME
    //

    private fun getMorningReminderTime(): Pair<Int,Int> {
        val totalMinutes = sharedPrefs.getString(MORNING_REMINDER_TIME, null)!!
        return parseTime(totalMinutes)
    }

    fun morningReminderTimeForTheSameDayAs(now: Calendar): Calendar {
        val (hour, minute) = getMorningReminderTime()
        val reminderCal = now.clone() as Calendar
        reminderCal.set(Calendar.HOUR_OF_DAY, hour)
        reminderCal.set(Calendar.MINUTE,      minute)
        reminderCal.set(Calendar.SECOND, 0)
        reminderCal.set(Calendar.MILLISECOND, 0)
        return reminderCal
    }

    //
    // IS_FIRST_DAY
    //

    fun displayReminderWhenLocked(): Boolean {
        return sharedPrefs.getBoolean(MORNING_REMINDER_LOCKSCREEN, true)
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