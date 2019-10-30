package com.example.thyroidhelper

import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager
import java.util.*

/*
 * The application state is defined by the last time the main button was pressed, and by app
 * settings. For simplicity, we store everything in the default SharedPreferences file.
 */
object DataModel {

    private const val DRUG_TAKEN_TIMESTAMP = "drug_taken_timestamp"
    private const val MEDICATION_TIME      = "medication_time"

    private fun getSettings(ctx: Context): SharedPreferences {
        return PreferenceManager.getDefaultSharedPreferences(ctx)
    }

// Public functions

    fun getDrugTakenTimestamp(ctx: Context): Long {
        return getSettings(ctx).getLong(DRUG_TAKEN_TIMESTAMP, 0)
    }

    fun setDrugTakenTimestamp(ctx: Context, timestamp: Long) {
        getSettings(ctx).edit()
            .putLong(DRUG_TAKEN_TIMESTAMP, timestamp)
            .apply()
        updateNotifications(ctx)
    }

    fun unsetDrugTakenTimestamp(ctx: Context) {
        getSettings(ctx).edit()
            .remove(DRUG_TAKEN_TIMESTAMP)
            .apply()
        updateNotifications(ctx)
    }

    fun currentTimestamp(): Long {
        return Calendar.getInstance().timeInMillis
    }

    fun hasTakenDrugToday(ctx: Context): Boolean {
        val today = Calendar.getInstance()
        today.set(Calendar.HOUR_OF_DAY, 0)
        today.set(Calendar.MINUTE, 0)
        today.set(Calendar.SECOND, 0)
        today.set(Calendar.MILLISECOND, 0)

        return today.timeInMillis <= getDrugTakenTimestamp(ctx)
    }

//

    fun getMedicineTimeHours(ctx: Context): Int {
        val str = getSettings(ctx).getString("medicine_time", null)
        val timeStr = str.split(":")[0]
        return Integer.parseInt(timeStr)
    }

    fun getMedicineTimeMinutes(ctx: Context): Int {
        val str = getSettings(ctx).getString("medicine_time", null)
        val timeStr = str.split(":")[1]
        return Integer.parseInt(timeStr)
    }

}