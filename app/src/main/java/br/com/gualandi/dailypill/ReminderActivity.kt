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

import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.WindowManager
import android.widget.Button
import androidx.appcompat.app.AlertDialog

class ReminderActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (DataModel.displayReminderWhenLocked()) {
            if (Build.VERSION.SDK_INT >= 27) {
                setShowWhenLocked(true)
            } else @Suppress("DEPRECATION") {
                window.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED)
            }
        }

        // This activity's title briefly flashes on the screen after we dismiss the dialog.
        // So we set it to the empty string as a workaround.
        setTitle("")

        // Don't call setContentView. We apparently don't need it
        // setContentView(/*...*/)

        val dialogContents = layoutInflater.inflate(R.layout.reminder_dialog, null)

        val dialog = AlertDialog.Builder(this)
            .setTitle(getString(R.string.reminder_title))
            .setView(dialogContents)
            .setNegativeButton(getString(R.string.reminder_snooze), null)
            .setOnCancelListener { finish() }
            .setOnDismissListener { finish() }
            .show()

        val okButton: Button = dialogContents.findViewById(R.id.button)
        okButton.setOnClickListener { clickOk(); dialog.dismiss() }
    }

    private fun clickOk() {
        if (!DataModel.hasTakenDrugToday()) {
            DataModel.takeDrugNow()
        }
        Notifications.possiblyCancelTheNotification()
    }
}