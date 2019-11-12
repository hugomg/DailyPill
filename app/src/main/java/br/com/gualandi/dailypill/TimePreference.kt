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
import android.content.res.TypedArray
import android.os.Build
import android.text.format.DateFormat
import android.util.AttributeSet
import androidx.preference.DialogPreference
import java.lang.IllegalArgumentException
import java.lang.NumberFormatException
import android.view.View
import android.widget.TimePicker
import androidx.preference.PreferenceDialogFragmentCompat

//
// We store the time as a human-readable string in HH:MM format.
//

fun serializeTime(t0: Int, t1: Int): String {
    return String.format("%02d:%02d", t0, t1)
}

fun parseTime(value: String): Pair<Int, Int> {
    val times = value.split(":")
    if (times.size != 2 ) { throw IllegalArgumentException("Invalid date format") }

    val t0 = try {
        Integer.parseInt(times[0])
    } catch (e: NumberFormatException) {
        throw IllegalArgumentException("Hours is not a number")
    }

    val t1 = try {
        Integer.parseInt(times[1])
    } catch (e: NumberFormatException) {
        throw IllegalArgumentException("Minutes is not a number")
    }

    if (t0 !in 0..23) { throw IllegalArgumentException("Hours is out of range") }
    if (t1 !in 0..59) { throw IllegalArgumentException("Minutes is out of range") }
    return Pair(t0, t1)
}

/** Select hours+minutes in settings screen with a TimePicker widget
 *
 * The code here is inspired by the implementation of the EditTextPreference, which can be found at
 * https://android.googlesource.com/platform/frameworks/support/+/androidx-master-dev/preference/preference/src/main/java/androidx/preference
 *
 * We also take inspiration from this StackOverflow answer:
 * https://stackoverflow.com/a/34398747
 *
 * I should note that the API for the androidx.preference library is slightly different from the
 * API for the deprecated android.preference library, and most of the examples on the web refer
 * to the old version. So watch out for that.
 */

class TimePreference : DialogPreference {

    public var hour   = 0
    public var minute = 0
    private var valueIsSet = false

    @Suppress("unused")
    constructor(context: Context): super(context)
    @Suppress("unused")
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)
    @Suppress("unused")
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int): super(context,attrs,defStyleAttr)
    @Suppress("unused")
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int, defStyleRes: Int): super(context, attrs, defStyleAttr, defStyleRes)

    override fun onGetDefaultValue(arr: TypedArray, i: Int): Any {
        return arr.getString(i) as Any
    }

    override fun onSetInitialValue(defaultValue: Any?) {
        val defaultString = (defaultValue ?: "00:00") as String
        val persisTedString = getPersistedString(defaultString)
        setTime(parseTime(persisTedString))
    }

    public fun setTime(value: Pair<Int, Int>) {
        // Always persist/notify the first time.
        val (newHour, newMinute) = value
        val changed = (hour != newHour || minute != newMinute)
        if (!valueIsSet || changed) {
            hour = newHour
            minute = newMinute
            persistString(serializeTime(newHour, newMinute))
        }
        if (changed) {
            notifyChanged()
        }
    }

    // override fun onSaveInstanceState(): Parcelable {}
    // override fun onRestoreInstanceState(state: Parcelable) {}
}

class TimePreferenceDialogFragmentCompat : PreferenceDialogFragmentCompat() {

    private lateinit var timePicker: TimePicker

    override fun onCreateDialogView(context: Context): View {
        timePicker = TimePicker(context)
        return timePicker
    }

    override fun onBindDialogView(view: View?) {
        super.onBindDialogView(view)
        timePicker.setIs24HourView(DateFormat.is24HourFormat(context))
        val pref = preference as TimePreference
        if (Build.VERSION.SDK_INT >= 23) {
            timePicker.hour   = pref.hour
            timePicker.minute = pref.minute
        } else @Suppress("DEPRECATION") {
            timePicker.currentHour   = pref.hour
            timePicker.currentMinute = pref.minute
        }
    }

    override fun onDialogClosed(positiveResult: Boolean) {
        if (positiveResult) {
            val pref = preference as TimePreference

            val newHour: Int
            val newMinute: Int
            if (Build.VERSION.SDK_INT >= 23) {
                newHour   = timePicker.hour
                newMinute = timePicker.minute
            } else @Suppress("DEPRECATION") {
                newHour   = timePicker.currentHour
                newMinute = timePicker.currentMinute
            }

            val value = Pair(newHour, newMinute)
            if (pref.callChangeListener(value)) {
                pref.setTime(value)
            }
        }
    }
}