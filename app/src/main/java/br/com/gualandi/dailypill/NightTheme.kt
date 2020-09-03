/*
 * Copyright © 2020 Hugo Musso Gualandi.
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
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatDelegate

// In order for the light/dark theme setting to take effect, we must initialize the theme at
// application startup, as well as any time that setting is changed.
object NightTheme : SharedPreferencesListener {

    private lateinit var appContext: Context

    fun init(context: Context) {
        appContext = context.applicationContext
        update()
        DataModel.addListener(this)
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        if (key.equals(DataModel.THEME_SETTING)) {
            update()
        }
    }

    public fun update() {
        val mode = when (DataModel.getTheme()) {
            "LIGHT" -> AppCompatDelegate.MODE_NIGHT_NO
            "DARK" -> AppCompatDelegate.MODE_NIGHT_YES
            "SYSTEM" -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
            else -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
        }
        AppCompatDelegate.setDefaultNightMode(mode);
    }
}