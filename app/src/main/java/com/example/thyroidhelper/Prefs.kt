package com.example.thyroidhelper

import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager
import java.util.*

//

private const val PREFS_FILE_NAME = "com.example.thyroidhelper"
private const val PREFS_DATE_KEY = "last_date"

private fun getPrefs(ctx: Context) : SharedPreferences{
    return ctx.getSharedPreferences(PREFS_FILE_NAME, Context.MODE_PRIVATE)
}

private fun getSettings(ctx: Context): SharedPreferences {
    return PreferenceManager.getDefaultSharedPreferences(ctx)
}

// Public functions

fun getDrugTakenTime(ctx: Context): Long {
    return getPrefs(ctx).getLong(PREFS_DATE_KEY, 0)
}

fun setDrugTakenTime(ctx: Context, timestamp: Long) {
    getPrefs(ctx).edit()
        .putLong(PREFS_DATE_KEY, timestamp)
        .apply()
    updateNotifications(ctx)
}

fun unsetDrugTakenTime(ctx: Context) {
    getPrefs(ctx).edit()
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