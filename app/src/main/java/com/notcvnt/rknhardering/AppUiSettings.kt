package com.notcvnt.rknhardering

import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat

object AppUiSettings {

    const val PREFS_NAME = "rknhardering_prefs"

    fun prefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun applySavedTheme(context: Context) {
        val prefs = prefs(context)
        SettingsActivity.applyTheme(prefs.getString(SettingsActivity.PREF_THEME, "system") ?: "system")
    }

    fun applyLanguage(languageTag: String) {
        AppCompatDelegate.setApplicationLocales(localeListForLanguageTag(languageTag))
    }

    fun localeListForLanguageTag(languageTag: String): LocaleListCompat {
        return if (languageTag.isBlank()) {
            LocaleListCompat.getEmptyLocaleList()
        } else {
            LocaleListCompat.forLanguageTags(languageTag)
        }
    }
}
