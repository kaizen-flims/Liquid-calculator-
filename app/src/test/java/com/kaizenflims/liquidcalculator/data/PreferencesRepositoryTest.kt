package com.kaizenflims.liquidcalculator.data

import androidx.datastore.preferences.core.mutablePreferencesOf
import com.kaizenflims.liquidcalculator.glass.GlassQuality
import com.kaizenflims.liquidcalculator.glass.GlassTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PreferencesRepositoryTest {
    @Test
    fun storedValuesDecodeIntoSettings() {
        val values = mutablePreferencesOf().apply {
            this[PreferenceKeys.glassIntensity] = 0.86f
            this[PreferenceKeys.motionEffects] = true
            this[PreferenceKeys.haptics] = false
            this[PreferenceKeys.quality] = GlassQuality.Balanced.name
            this[PreferenceKeys.theme] = GlassTheme.DeepBlue.name
            this[PreferenceKeys.wallpaperUri] = "content://wallpaper/1"
            this[PreferenceKeys.wallpaperZoom] = 2.2f
            this[PreferenceKeys.wallpaperOffsetX] = -0.4f
            this[PreferenceKeys.wallpaperOffsetY] = 0.3f
        }

        val decoded = PreferencesRepository.decode(values)

        assertEquals(0.86f, decoded.glassIntensity)
        assertTrue(decoded.motionEffects)
        assertEquals(false, decoded.haptics)
        assertEquals(GlassQuality.Balanced, decoded.quality)
        assertEquals(GlassTheme.DeepBlue, decoded.theme)
        assertEquals("content://wallpaper/1", decoded.wallpaperUri)
        assertEquals(2.2f, decoded.wallpaperZoom)
        assertEquals(-0.4f, decoded.wallpaperOffsetX)
        assertEquals(0.3f, decoded.wallpaperOffsetY)
    }

    @Test
    fun corruptEnumNamesFallBackSafely() {
        val values = mutablePreferencesOf().apply {
            this[PreferenceKeys.quality] = "impossible"
            this[PreferenceKeys.theme] = "missing"
        }

        val decoded = PreferencesRepository.decode(values)

        assertEquals(GlassQuality.Adaptive, decoded.quality)
        assertEquals(GlassTheme.Clear, decoded.theme)
    }
}
