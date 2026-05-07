package com.notcvnt.rknhardering

import org.junit.Assert.assertEquals
import org.junit.Test

class LauncherIconVariantTest {

    @Test
    fun `maps color vision modes to launcher icon variants`() {
        assertEquals(
            LauncherIconVariant.ORIGINAL,
            LauncherIconVariant.fromCvd(ColorVisionMode.OFF, LauncherIconVariant.PROTANOPIA),
        )
        assertEquals(
            LauncherIconVariant.DEUTERANOPIA,
            LauncherIconVariant.fromCvd(ColorVisionMode.RED_GREEN, null),
        )
        assertEquals(
            LauncherIconVariant.PROTANOPIA,
            LauncherIconVariant.fromCvd(ColorVisionMode.RED_GREEN, LauncherIconVariant.PROTANOPIA),
        )
        assertEquals(
            LauncherIconVariant.DEUTERANOPIA,
            LauncherIconVariant.fromCvd(ColorVisionMode.RED_GREEN, LauncherIconVariant.DEUTERANOPIA),
        )
        assertEquals(
            LauncherIconVariant.DEUTERANOPIA,
            LauncherIconVariant.fromCvd(ColorVisionMode.RED_GREEN, LauncherIconVariant.ORIGINAL),
        )
        assertEquals(
            LauncherIconVariant.TRITANOPIA,
            LauncherIconVariant.fromCvd(ColorVisionMode.BLUE_YELLOW, LauncherIconVariant.PROTANOPIA),
        )
        assertEquals(
            LauncherIconVariant.MONOCHROME,
            LauncherIconVariant.fromCvd(ColorVisionMode.ACHROMATOPSIA, LauncherIconVariant.PROTANOPIA),
        )
    }
}
