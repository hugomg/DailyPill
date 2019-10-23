package com.example.thyroidhelper

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AlertDialog
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.TextView
import java.text.DateFormat

const val fadeDuration = 150L

enum class AppState {
    START, DRUG_NOT_TAKEN, DRUG_TAKEN,
}

class MainActivity : AppCompatActivity() {

    private var state = AppState.START
    private var isAnimating = false

    private lateinit var buttonView: Button
    private lateinit var drugNotTakenMessageView: TextView
    private lateinit var drugTakenMessageView: TextView

    private lateinit var drugTakenMessage: String

    private var resetMenuItem: MenuItem? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setSupportActionBar(findViewById(R.id.toolbar))

        buttonView              = findViewById(R.id.button)
        drugNotTakenMessageView = findViewById(R.id.not_taken_message)
        drugTakenMessageView    = findViewById(R.id.taken_message)

        drugTakenMessage = resources.getString(R.string.drug_taken_message)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        resetMenuItem = menu.findItem(R.id.action_reset)
        resetMenuItem!!.isEnabled = (state == AppState.DRUG_TAKEN)
        return true
    }

    override fun onResume() {
        if (hasTakenDrugToday(this)) {
            gotoDrugTaken(true)
        } else {
            gotoDrugNotTaken(true)
        }

        super.onResume()
    }

    //
    // State changing
    //

    private fun gotoDrugTaken(instant: Boolean) {
        val timestamp = getDrugTakenTime(this)
        val timeStr = DateFormat.getTimeInstance(DateFormat.SHORT).format(timestamp)
        // Use non-breaking space to avoid a line-break between 6:00 and AM
        val nbspTimeStr = timeStr.replace(" ", "\u00A0" )
        drugTakenMessageView.text = String.format(drugTakenMessage, nbspTimeStr)

        if (state == AppState.DRUG_TAKEN) { return }
        state = AppState.DRUG_TAKEN

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
        drugTakenMessageView.alpha    = 1.0f

        buttonView.visibility              = View.GONE
        drugNotTakenMessageView.visibility = View.GONE
        drugTakenMessageView.visibility    = View.VISIBLE

        resetMenuItem?.isEnabled = true
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
        buttonView.alpha              = 1.0f
        drugNotTakenMessageView.alpha = 1.0f

        buttonView.visibility              = View.VISIBLE
        drugNotTakenMessageView.visibility = View.VISIBLE
        drugTakenMessageView.visibility    = View.GONE
        resetMenuItem?.isEnabled = false
    }

    // Menu action
    private fun performReset() {
        if (state == AppState.DRUG_NOT_TAKEN) return

        val dialog = AlertDialog.Builder(this)
            .setTitle(R.string.reset_confirmation_title)
            .setMessage(R.string.reset_confirmation_message)
            .setPositiveButton(R.string.reset_confirmation_ok) { _, _ ->
                    unsetDrugTakenTime(this)
                    gotoDrugNotTaken(false)
                }
            .setNegativeButton(R.string.reset_confirmation_cancel,null)
            .create()
        dialog.show()
    }

    private fun performReminder() {
        val intent = Intent(this, ReminderActivity::class.java)
        startActivity(intent)
    }


    // Onclick handler for the button
    @Suppress("UNUSED_PARAMETER")
    fun performUpdateTime(btn: View) {
        if (isAnimating) return
        setDrugTakenTime(this, currentTime())
        gotoDrugTaken(false)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_reset -> {
                performReset()
                return true
            }
            R.id.action_reminder -> {
                performReminder()
                return true
            }
            else -> {
                return super.onOptionsItemSelected(item)
            }
        }
    }

}
