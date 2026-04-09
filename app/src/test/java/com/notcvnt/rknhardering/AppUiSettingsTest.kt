package com.notcvnt.rknhardering

import org.junit.Assert.assertEquals
import org.junit.Test

class AppUiSettingsTest {

    @Test
    fun `builds locale list for saved language tag`() {
        assertEquals("ru", AppUiSettings.localeListForLanguageTag("ru").toLanguageTags())
    }

    @Test
    fun `returns empty locale list for system language`() {
        assertEquals("", AppUiSettings.localeListForLanguageTag("").toLanguageTags())
    }
}
