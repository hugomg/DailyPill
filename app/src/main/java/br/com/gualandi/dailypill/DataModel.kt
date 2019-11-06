package br.com.gualandi.dailypill

import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import java.util.*

typealias SharedPreferencesListener = SharedPreferences.OnSharedPreferenceChangeListener

object DataModel {

    const val DRUG_TAKEN_TIMESTAMP = "drug_taken_timestamp"
    const val REMINDER_ENABLED     = "reminder_enabled"
    const val REMINDER_TIME        = "reminder_time"
    const val REMINDER_LOCK_SCREEN = "reminder_lock_screen"

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