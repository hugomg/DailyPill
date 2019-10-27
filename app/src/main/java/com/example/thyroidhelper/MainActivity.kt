package com.example.thyroidhelper

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AlertDialog
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction

class MainActivity : AppCompatActivity() {

    private var resetMenuItem: MenuItem? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setSupportActionBar(findViewById(R.id.toolbar))
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        resetMenuItem = menu.findItem(R.id.action_reset)
        resetMenuItem?.isEnabled = hasTakenDrugToday(this)
        return true
    }

    override fun onResume() {
        if (hasTakenDrugToday(this)) {
            gotoDrugTaken(false)
        } else {
            gotoDrugNotTaken(false)
        }
        super.onResume()
    }

    private fun switchFragment(useFade: Boolean, fragment: Fragment) {
        supportFragmentManager.beginTransaction().apply {
            replace(R.id.fragment_container, fragment)
            if (useFade) { setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE) }
            commit()
        }
    }

    private fun gotoDrugTaken(useFade: Boolean) {
        resetMenuItem?.isEnabled = true
        switchFragment(useFade, MedicineTakenFragment())
    }

    private fun gotoDrugNotTaken(useFade: Boolean) {
        resetMenuItem?.isEnabled = false
        switchFragment(useFade, MedicineNotTakenFragment())
    }

    @Suppress("UNUSED_PARAMETER")
    fun performUpdateTime(btn: View) {
        setDrugTakenTime(this, currentTime())
        gotoDrugTaken(true)
    }

    private fun doReset() {
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

    private fun doReminder() {
        val intent = Intent(this, ReminderActivity::class.java)
        startActivity(intent)
    }

    private fun doAddNotification() {
        sendMorningReminderNotification(this)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_reset -> {
                doReset()
                return true
            }
            R.id.action_reminder -> {
                doReminder()
                return true
            }
            R.id.action_register_notification -> {
                doAddNotification()
                return true
            }
        }
        // Fallback
        return super.onOptionsItemSelected(item)
    }
}
