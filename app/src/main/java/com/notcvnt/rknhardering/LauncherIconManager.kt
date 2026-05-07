package com.notcvnt.rknhardering

import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log

internal object LauncherIconManager {
    private const val TAG = "LauncherIconManager"

    internal var setComponentEnabledSettingForTests:
        ((PackageManager, ComponentName, Int) -> Unit)? = null

    fun apply(context: Context, target: LauncherIconVariant): Boolean {
        return try {
            val pm = context.packageManager
            val pkg = context.packageName

            setStateIfNeeded(pm, ComponentName(pkg, target.aliasClass), PackageManager.COMPONENT_ENABLED_STATE_ENABLED)
            LauncherIconVariant.entries
                .filterNot { it == target }
                .forEach { variant ->
                    setStateIfNeeded(
                        pm,
                        ComponentName(pkg, variant.aliasClass),
                        PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                    )
                }
            true
        } catch (e: SecurityException) {
            Log.w(TAG, "Failed to toggle launcher alias", e)
            false
        }
    }

    fun current(context: Context): LauncherIconVariant {
        val pm = context.packageManager
        val pkg = context.packageName
        return LauncherIconVariant.entries.firstOrNull { variant ->
            pm.getComponentEnabledSetting(ComponentName(pkg, variant.aliasClass)) ==
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED
        } ?: LauncherIconVariant.ORIGINAL
    }

    private fun setStateIfNeeded(
        pm: PackageManager,
        component: ComponentName,
        newState: Int,
    ) {
        if (pm.getComponentEnabledSetting(component) != newState) {
            setComponentEnabledSettingForTests?.invoke(pm, component, newState)
                ?: pm.setComponentEnabledSetting(component, newState, PackageManager.DONT_KILL_APP)
        }
    }
}
