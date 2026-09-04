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
        val values = mutablePreferencesOf(
            PreferencesRepository.Keys.glassIntensity to 0.86f,
            PreferencesRepository.Keys.motionEffects to true,
            PreferencesRepository.Keys.haptics to false,
            PreferencesRepository.Keys.quality to GlassQuality.Balanced.name,
            PreferencesRepository.Keys.theme to GlassTheme.DeepBlue.name,
            PreferencesRepository.Keys.wallpaperUri to "content://wallpaper/1",
            PreferencesRepository.Keys.wallpaperZoom to 2.2f,
            PreferencesRepository.Keys.wallpaperOffsetX to -0.4f,
            PreferencesRepository.Keys.wallpaperOffsetY to 0.3f,
        )

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
        val values = mutablePreferencesOf(
            PreferencesRepository.Keys.quality to "impossible",
            PreferencesRepository.Keys.theme to "missing",
        )

        val decoded = PreferencesRepository.decode(values)

        assertEquals(GlassQuality.Adaptive, decoded.quality)
        assertEquals(GlassTheme.Clear, decoded.theme)
    }
}

