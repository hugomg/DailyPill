package com.example.thyroidhelper

import android.content.Context
import android.content.SharedPreferences
import java.util.*

//

private const val PREFS_FILE_NAME = "com.example.thyroidhelper.preferences"
private const val PREFS_DATE_KEY = "last_date"

private fun getPrefs(ctx: Context) : SharedPreferences{
    return ctx.getSharedPreferences(PREFS_FILE_NAME, Context.MODE_PRIVATE)
}

// Public functions

fun getDrugTakenTime(ctx: Context): Long {
    return getPrefs(ctx).getLong(PREFS_DATE_KEY, 0)
}

fun setDrugTakenTime(ctx: Context, timestamp: Long) {
    getPrefs(ctx).edit()
        .putLong(PREFS_DATE_KEY, timestamp)
        .apply()
}

fun unsetDrugTakenTime(ctx: Context) {
    getPrefs(ctx).edit()
        .remove(PREFS_DATE_KEY)
        .apply()
}

fun hasTakenDrugToday(ctx: Context): Boolean {
    val today = Calendar.getInstance()
    today.set(Calendar.HOUR_OF_DAY, 0)
    today.set(Calendar.MINUTE, 0)
    today.set(Calendar.SECOND, 0)
    today.set(Calendar.MILLISECOND, 0)

    return today.timeInMillis <= getDrugTakenTime(ctx)
}
