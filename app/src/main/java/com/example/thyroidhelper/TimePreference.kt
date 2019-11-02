package com.example.thyroidhelper

import android.content.Context
import android.content.res.TypedArray
import android.text.format.DateFormat
import android.util.AttributeSet
import androidx.preference.DialogPreference
import java.lang.IllegalArgumentException
import java.lang.NumberFormatException
import android.view.View
import android.widget.TimePicker
import androidx.preference.PreferenceDialogFragmentCompat

/*
 * Internally, we serialize the time in a human-readable string in HH:MM format, to allow us to
 * use that format when setting the defaultValue attribute.
 */

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

    if (! (0 <= t0 && t0 < 24)) { throw IllegalArgumentException("Hours is out of range") }
    if (! (0 <= t1 && t1 < 60)) { throw IllegalArgumentException("Minutes is out of range") }
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
 * to the old version. One of the differences is that the old version has an OldCreateDialogView
 * method, that isn't present in the androidx version of the library.
 */
class TimePreference : DialogPreference {

    public var hour   = 0
    public var minute = 0
    private var valueIsSet = false

    constructor(context: Context): super(context) {}
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {}
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int): super(context,attrs,defStyleAttr) {}
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int, defStyleRes: Int): super(context, attrs, defStyleAttr, defStyleRes) {}

    override fun onGetDefaultValue(arr: TypedArray, i: Int): Any {
        return arr.getString(i) as Any
    }

    override fun onSetInitialValue(defaultValue: Any?) {
        val defaultString = (defaultValue ?: "00:00") as String
        val persisTedString = getPersistedString(defaultString)
        val time = parseTime(persisTedString)
        hour   = time.first
        minute = time.second
    }

    public fun setTime(value: Pair<Int, Int>) {
        // Always persist/notify the first time.
        val (newHour, newMinute) = value
        val changed = (hour != newHour || minute != newMinute)
        if (changed || !valueIsSet) {
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

    override fun onCreateDialogView(context: Context?): View {
        timePicker = TimePicker(context)
        return timePicker
    }

    override fun onBindDialogView(view: View?) {
        super.onBindDialogView(view)
        timePicker.setIs24HourView(DateFormat.is24HourFormat(context))
        val pref = preference as TimePreference
        timePicker.currentHour   = pref.hour
        timePicker.currentMinute = pref.minute
    }

    override fun onDialogClosed(positiveResult: Boolean) {
        if (positiveResult) {
            val pref = preference as TimePreference
            val value = Pair(timePicker.currentHour, timePicker.currentMinute)
            if (pref.callChangeListener(value)) {
                pref.setTime(value)
            }
        }
    }
}