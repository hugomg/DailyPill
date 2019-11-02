package com.example.thyroidhelper

import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager
import java.util.*

typealias SharedPreferencesListener = SharedPreferences.OnSharedPreferenceChangeListener

object DataModel {

    const val IS_FIRST_DAY         = "is_first_day"
    const val DRUG_TAKEN_TIMESTAMP = "drug_taken_timestamp"
    const val MEDICATION_TIME      = "medication_time"

    private lateinit var sharedPrefs: SharedPreferences
    private lateinit var defaultMedicationTime: String

    /**
     * This should be called on application startup, before anything else.
     */
    fun init(context: Context) {
        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context)
        defaultMedicationTime = context.getString(R.string.preferences_default_medication_time)
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
    // MEDICATION_TIME
    //

    fun getMedicationTime(): Pair<Int,Int> {
        val str = sharedPrefs.getString(MEDICATION_TIME, defaultMedicationTime)!!
        return parseTime(str)
    }

    fun medicationTimeForTheSameDayAs(now: Calendar): Calendar {
        val (hour, minute) = getMedicationTime()
        val medicationCal = now.clone() as Calendar
        medicationCal.set(Calendar.HOUR_OF_DAY, hour)
        medicationCal.set(Calendar.MINUTE,      minute)
        medicationCal.set(Calendar.SECOND, 0)
        medicationCal.set(Calendar.MILLISECOND, 0)
        return medicationCal
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