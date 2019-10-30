package com.example.thyroidhelper

import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager
import java.util.*

typealias SharedPreferencesListener = SharedPreferences.OnSharedPreferenceChangeListener

object DataModel {

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
    // DRUG_TAKEN_TIMESTAMP
    //

    fun getDrugTakenTimestamp(): Long {
        return sharedPrefs.getLong(DRUG_TAKEN_TIMESTAMP, 0)
    }

    fun setDrugTakenTimestamp(timestamp: Long) {
        sharedPrefs.edit()
            .putLong(DRUG_TAKEN_TIMESTAMP, timestamp)
            .apply()
    }

    fun unsetDrugTakenTimestamp() {
        sharedPrefs.edit()
            .remove(DRUG_TAKEN_TIMESTAMP)
            .apply()
    }

    fun currentTimestamp(): Long {
        return Calendar.getInstance().timeInMillis
    }

    fun hasTakenDrugToday(): Boolean {
        val today = Calendar.getInstance()
        today.set(Calendar.HOUR_OF_DAY, 0)
        today.set(Calendar.MINUTE, 0)
        today.set(Calendar.SECOND, 0)
        today.set(Calendar.MILLISECOND, 0)

        return today.timeInMillis <= getDrugTakenTimestamp()
    }

    //
    // MEDICATION_TIME
    //

    fun getMedicationTimeHours(): Int {
        val str = sharedPrefs.getString(MEDICATION_TIME, defaultMedicationTime)!!
        val timeStr = str.split(":")[0]
        return Integer.parseInt(timeStr)
    }

    fun getMedicationTimeMinutes(): Int {
        val str = sharedPrefs.getString(MEDICATION_TIME, defaultMedicationTime)!!
        val timeStr = str.split(":")[1]
        return Integer.parseInt(timeStr)
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