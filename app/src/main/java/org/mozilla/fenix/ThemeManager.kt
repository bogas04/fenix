/* This Source Code Form is subject to the terms of the Mozilla Public
   License, v. 2.0. If a copy of the MPL was not distributed with this
   file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.fenix

import android.app.Activity
import android.content.Context
import android.util.TypedValue
import android.view.View
import android.view.Window
import androidx.core.content.ContextCompat

interface ThemeManager {
    enum class Theme {
        Light, Private
    }

    val currentTheme: Theme
    fun setTheme(theme: Theme)
}

fun Activity.setTheme(theme: ThemeManager.Theme) {
    val themeCode = when (theme) {
        ThemeManager.Theme.Light -> R.style.LightTheme
        ThemeManager.Theme.Private -> R.style.PrivateTheme
    }

    setTheme(themeCode)
}

fun ThemeManager.Theme.isPrivate(): Boolean = this == ThemeManager.Theme.Private

private var temporaryThemeManagerStorage = ThemeManager.Theme.Light
class DefaultThemeManager : ThemeManager {
    var onThemeChange: ((ThemeManager.Theme) -> Unit)? = null

    override val currentTheme: ThemeManager.Theme
        get() = temporaryThemeManagerStorage

    override fun setTheme(theme: ThemeManager.Theme) {
        temporaryThemeManagerStorage = theme

        onThemeChange?.invoke(currentTheme)
    }

    companion object {
        fun resolveAttribute(attribute: Int, context: Context): Int {
            val typedValue = TypedValue()
            val theme = context.theme
            theme.resolveAttribute(attribute, typedValue, true)

            return typedValue.resourceId
        }

        // Handles status bar theme change since the window does not dynamically recreate
        fun applyStatusBarTheme(window: Window, themeManager: ThemeManager, context: Context) {
            window.statusBarColor = ContextCompat
                .getColor(context, DefaultThemeManager
                    .resolveAttribute(android.R.attr.statusBarColor, context))

            window.navigationBarColor = ContextCompat
                .getColor(context, DefaultThemeManager
                    .resolveAttribute(android.R.attr.navigationBarColor, context))

            when (themeManager.currentTheme) {
                ThemeManager.Theme.Light -> {
                    window.decorView.systemUiVisibility = window.decorView.systemUiVisibility or
                            View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR or View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
                }
                ThemeManager.Theme.Private -> {
                    window.decorView.systemUiVisibility = window.decorView.systemUiVisibility and
                            View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR.inv() and
                            View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR.inv()
                }
            }
        }
    }
}
