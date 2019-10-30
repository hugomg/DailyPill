package com.example.thyroidhelper

import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager
import java.util.*

/*
 * The application state is defined by the last time the main button was pressed, and by app
 * settings. For simplicity, we store everything in the default SharedPreferences file.
 */

//

private const val PREFS_DATE_KEY = "drug_taken_timestamp"

private fun getSettings(ctx: Context): SharedPreferences {
    return PreferenceManager.getDefaultSharedPreferences(ctx)
}

// Public functions

fun getDrugTakenTime(ctx: Context): Long {
    return getSettings(ctx).getLong(PREFS_DATE_KEY, 0)
}

fun setDrugTakenTime(ctx: Context, timestamp: Long) {
    getSettings(ctx).edit()
        .putLong(PREFS_DATE_KEY, timestamp)
        .apply()
    updateNotifications(ctx)
}

fun unsetDrugTakenTime(ctx: Context) {
    getSettings(ctx).edit()
        .remove(PREFS_DATE_KEY)
        .apply()
    updateNotifications(ctx)
}

fun currentTime(): Long {
    return Calendar.getInstance().timeInMillis
}

fun hasTakenDrugToday(ctx: Context): Boolean {
    val today = Calendar.getInstance()
    today.set(Calendar.HOUR_OF_DAY, 0)
    today.set(Calendar.MINUTE, 0)
    today.set(Calendar.SECOND, 0)
    today.set(Calendar.MILLISECOND, 0)

    return today.timeInMillis <= getDrugTakenTime(ctx)
}

//

fun getMedicineTimeHours(ctx: Context) : Int {
    val str = getSettings(ctx).getString("medicine_time", null)
    val timeStr = str.split(":")[0]
    return Integer.parseInt(timeStr)
}

fun getMedicineTimeMinutes(ctx: Context) : Int {
    val str = getSettings(ctx).getString("medicine_time", null)
    val timeStr = str.split(":")[1]
    return Integer.parseInt(timeStr)
}