package com.notcvnt.rknhardering

import android.app.Application
import android.content.SharedPreferences
import androidx.core.content.edit
import com.notcvnt.rknhardering.probe.NativeCurlBridge

class RknHarderingApp : Application() {

    override fun onCreate() {
        super.onCreate()
        NativeCurlBridge.initIfNeeded(this)
        AppUiSettings.applySavedTheme(this)
        migrateLauncherIconIfNeeded()
    }

    internal fun migrateLauncherIconIfNeeded() {
        val prefs = AppUiSettings.prefs(this)
        if (prefs.getBoolean(SettingsPrefs.PREF_ICON_MIGRATION_DONE, false)) return

        val rawMode = prefs.getString(SettingsPrefs.PREF_COLOR_VISION_MODE, null)
        syncLegacyRedGreenSubVariant(prefs, rawMode)

        val mode = ColorVisionMode.fromPref(rawMode)
        val ok = if (mode == ColorVisionMode.OFF) {
            true
        } else {
            LauncherIconManager.apply(
                this,
                LauncherIconVariant.fromCvd(mode, redGreenSubVariantForMigration(prefs, rawMode)),
            )
        }
        if (ok) {
            prefs.edit { putBoolean(SettingsPrefs.PREF_ICON_MIGRATION_DONE, true) }
        }
    }

    private fun syncLegacyRedGreenSubVariant(prefs: SharedPreferences, rawMode: String?) {
        if (rawMode != LauncherIconVariant.PROTANOPIA.prefValue) return
        prefs.edit {
            putBoolean(SettingsPrefs.PREF_EASTER_EGG_PROTANOPIA_UNLOCKED, true)
            putString(SettingsPrefs.PREF_RED_GREEN_ICON_VARIANT, LauncherIconVariant.PROTANOPIA.prefValue)
        }
    }

    private fun redGreenSubVariantForMigration(
        prefs: SharedPreferences,
        rawMode: String?,
    ): LauncherIconVariant {
        if (rawMode == LauncherIconVariant.PROTANOPIA.prefValue) {
            return LauncherIconVariant.PROTANOPIA
        }
        val unlocked = prefs.getBoolean(SettingsPrefs.PREF_EASTER_EGG_PROTANOPIA_UNLOCKED, false)
        val storedVariant = prefs.getString(
            SettingsPrefs.PREF_RED_GREEN_ICON_VARIANT,
            LauncherIconVariant.DEUTERANOPIA.prefValue,
        )
        return if (unlocked && storedVariant == LauncherIconVariant.PROTANOPIA.prefValue) {
            LauncherIconVariant.PROTANOPIA
        } else {
            LauncherIconVariant.DEUTERANOPIA
        }
    }
}
