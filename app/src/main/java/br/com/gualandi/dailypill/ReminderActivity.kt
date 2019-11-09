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

import android.content.SharedPreferences
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Button

class ReminderActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reminder)
        setTitle(R.string.reminder_title)

        if (DataModel.displayReminderWhenLocked()) {
            if (Build.VERSION.SDK_INT >= 27) {
                setShowWhenLocked(true)
            } else @Suppress("DEPRECATION") {
                window.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED)
            }
        }

        // Force the button to use the whole available width. By default the width is set to
        // WRAP_CONTENT, and apparently only changing it here works. Setting the layout size on the
        // XML file is not enough.
        window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)

        val okButton: Button = findViewById(R.id.ok_button)!!
        okButton.setOnClickListener { clickOK() }

        val cancelButton: Button = findViewById(R.id.cancel_button)!!
        cancelButton.setOnClickListener { dismiss() }
    }

    private fun clickOK() {
        if (!DataModel.hasTakenDrugToday()) {
            DataModel.takeDrugNow()
        }
        Notifications.possiblyCancelTheNotification()
        finish()
    }

    private fun dismiss() {
        finish()
    }
}