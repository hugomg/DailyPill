package com.example.thyroidhelper

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.content.SharedPreferences
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.TextView
import java.text.DateFormat
import java.util.*

//import java.time.LocalDateTime
//import java.time.format.DateTimeFormatter
//import java.time.format.FormatStyle

const val PREFS_FILE_NAME = "com.example.thyroidhelper.preferences"
const val PREFS_DATE_KEY = "last_date"

const val fadeDuration = 150L

enum class AppState {
    START, DRUG_NOT_TAKEN, DRUG_TAKEN,
}

class MainActivity : AppCompatActivity() {

    var state = AppState.START
    var isAnimating = false

    lateinit var preferences: SharedPreferences
    lateinit var buttonView: Button
    lateinit var drugNotTakenMessageView: TextView
    lateinit var drugTakenMessageView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setSupportActionBar(findViewById(R.id.toolbar))

        preferences = this.getSharedPreferences(PREFS_FILE_NAME, Context.MODE_PRIVATE)

        buttonView              = findViewById(R.id.button)
        drugNotTakenMessageView = findViewById(R.id.not_taken_message)
        drugTakenMessageView    = findViewById(R.id.taken_message)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onResume() {
        if (hasTakenDrugToday()) {
            gotoDrugTaken(true)
        } else {
            gotoDrugNotTaken(true)
        }

        super.onResume()
    }

    //
    // Data
    //

    private fun getDrugTakenTime(): Long {
        return preferences.getLong(PREFS_DATE_KEY, 0)
    }

    private fun setDrugTakenTime(timestamp: Long) {
        preferences.edit()
            .putLong(PREFS_DATE_KEY, timestamp)
            .apply()
    }

    private fun unsetDrugTakenTime() {
        preferences.edit()
            .remove(PREFS_DATE_KEY)
            .apply()
    }

    private fun hasTakenDrugToday(): Boolean {
        val today = Calendar.getInstance()
        today.set(Calendar.HOUR_OF_DAY, 0)
        today.set(Calendar.MINUTE, 0)
        today.set(Calendar.SECOND, 0)
        today.set(Calendar.MILLISECOND, 0)

        return today.timeInMillis <= getDrugTakenTime()
    }

    //
    // State changing
    //

    private fun gotoDrugTaken(instant: Boolean) {

        if (state == AppState.DRUG_TAKEN) { return }
        state = AppState.DRUG_TAKEN

        val time = DateFormat.getTimeInstance(DateFormat.SHORT).format(getDrugTakenTime())
        drugTakenMessageView.text = String.format("Remédio tomado\n%s", time)

        if (instant) {
            finishGotoDrugTaken()
        } else {
            isAnimating = true

            buttonView.visibility = View.VISIBLE
            buttonView.alpha = 1.0f
            buttonView.animate()
                .alpha(0.0f)
                .setDuration(fadeDuration)

            drugNotTakenMessageView.visibility = View.VISIBLE
            drugNotTakenMessageView.alpha = 1.0f
            drugNotTakenMessageView.animate()
                .alpha(0.0f)
                .setDuration(fadeDuration)


            drugTakenMessageView.visibility = View.VISIBLE
            drugTakenMessageView.alpha = 0.0f
            drugTakenMessageView.animate()
                .setStartDelay(fadeDuration)
                .alpha(1.0f)
                .setDuration(fadeDuration)
                .setListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator) {
                        isAnimating = false
                        finishGotoDrugTaken()
                    }
                })
        }
    }

    private fun finishGotoDrugTaken() {
        buttonView.visibility              = View.GONE
        drugNotTakenMessageView.visibility = View.GONE
        drugTakenMessageView.visibility    = View.VISIBLE
    }

    private fun gotoDrugNotTaken(instant: Boolean) {
        if (state == AppState.DRUG_NOT_TAKEN) return
        state = AppState.DRUG_NOT_TAKEN

        if (instant) {
            finishGotoDrugNotTaken()
        } else {
            isAnimating = true

            drugTakenMessageView.visibility = View.VISIBLE
            drugTakenMessageView.alpha = 1.0f
            drugTakenMessageView.animate()
                .alpha(0.0f)
                .setDuration(fadeDuration)

            drugNotTakenMessageView.visibility = View.VISIBLE
            drugNotTakenMessageView.alpha = 0.0f
            drugNotTakenMessageView.animate()
                .setStartDelay(fadeDuration)
                .alpha(1.0f)
                .setDuration(fadeDuration)

            buttonView.visibility = View.VISIBLE
            buttonView.alpha = 0.0f
            buttonView.animate()
                .setStartDelay(fadeDuration)
                .alpha(1.0f)
                .setDuration(fadeDuration)
                .setListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator) {
                        isAnimating = false
                        finishGotoDrugNotTaken()
                    }
                })
        }
    }

    private fun finishGotoDrugNotTaken() {
        buttonView.visibility              = View.VISIBLE
        drugNotTakenMessageView.visibility = View.VISIBLE
        drugTakenMessageView.visibility    = View.GONE
    }

    // Menu action
    private fun forgetLastTime() {
        unsetDrugTakenTime()
        gotoDrugNotTaken(false)
    }

    // Onclick handler for the button
    fun updateTime(btn: View) {
        if (isAnimating) return
        val timestamp = Calendar.getInstance().timeInMillis
        setDrugTakenTime(timestamp)
        gotoDrugTaken(false)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_reset -> {
                forgetLastTime()
                return true
            }
            else -> {
                return super.onOptionsItemSelected(item)
            }
        }
    }

}
