package com.notcvnt.rknhardering

import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.edit
import androidx.test.core.app.ApplicationProvider
import com.notcvnt.rknhardering.probe.NativeCurlBridge
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class RknHarderingAppTest {

    private val app: RknHarderingApp = ApplicationProvider.getApplicationContext()

    @Before
    fun setUp() {
        NativeCurlBridge.resetForTests()
        AppUiSettings.prefs(app).edit().clear().commit()
        resetLauncherAliases()
        LauncherIconManager.setComponentEnabledSettingForTests = null
    }

    @After
    fun tearDown() {
        NativeCurlBridge.resetForTests()
        LauncherIconManager.setComponentEnabledSettingForTests = null
        resetLauncherAliases()
    }

    @Test
    fun `first launch migrates blue yellow mode to tritanopia icon`() {
        AppUiSettings.prefs(app).edit {
            putString(SettingsPrefs.PREF_COLOR_VISION_MODE, ColorVisionMode.BLUE_YELLOW.prefValue)
        }

        app.migrateLauncherIconIfNeeded()

        assertEquals(LauncherIconVariant.TRITANOPIA, LauncherIconManager.current(app))
        assertTrue(AppUiSettings.prefs(app).getBoolean(SettingsPrefs.PREF_ICON_MIGRATION_DONE, false))
    }

    @Test
    fun `migration preserves legacy protanopia preference`() {
        AppUiSettings.prefs(app).edit {
            putString(SettingsPrefs.PREF_COLOR_VISION_MODE, LauncherIconVariant.PROTANOPIA.prefValue)
        }

        app.migrateLauncherIconIfNeeded()

        assertEquals(LauncherIconVariant.PROTANOPIA, LauncherIconManager.current(app))
        assertTrue(
            AppUiSettings.prefs(app)
                .getBoolean(SettingsPrefs.PREF_EASTER_EGG_PROTANOPIA_UNLOCKED, false),
        )
        assertEquals(
            LauncherIconVariant.PROTANOPIA.prefValue,
            AppUiSettings.prefs(app).getString(SettingsPrefs.PREF_RED_GREEN_ICON_VARIANT, null),
        )
    }

    @Test
    fun `second launch does not rerun migration when flag is set`() {
        AppUiSettings.prefs(app).edit {
            putString(SettingsPrefs.PREF_COLOR_VISION_MODE, ColorVisionMode.BLUE_YELLOW.prefValue)
            putBoolean(SettingsPrefs.PREF_ICON_MIGRATION_DONE, true)
        }

        app.migrateLauncherIconIfNeeded()

        assertEquals(LauncherIconVariant.ORIGINAL, LauncherIconManager.current(app))
    }

    @Test
    fun `failed icon migration is retried on next launch`() {
        AppUiSettings.prefs(app).edit {
            putString(SettingsPrefs.PREF_COLOR_VISION_MODE, ColorVisionMode.BLUE_YELLOW.prefValue)
        }
        LauncherIconManager.setComponentEnabledSettingForTests = { _, _, _ ->
            throw SecurityException("blocked")
        }

        app.migrateLauncherIconIfNeeded()

        assertFalse(AppUiSettings.prefs(app).getBoolean(SettingsPrefs.PREF_ICON_MIGRATION_DONE, false))
    }

    private fun resetLauncherAliases() {
        LauncherIconVariant.entries.forEach { variant ->
            app.packageManager.setComponentEnabledSetting(
                ComponentName(app.packageName, variant.aliasClass),
                PackageManager.COMPONENT_ENABLED_STATE_DEFAULT,
                PackageManager.DONT_KILL_APP,
            )
        }
    }
}
