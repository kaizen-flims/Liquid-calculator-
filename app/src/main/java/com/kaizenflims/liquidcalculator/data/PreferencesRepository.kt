package com.kaizenflims.liquidcalculator.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.kaizenflims.liquidcalculator.glass.GlassQuality
import com.kaizenflims.liquidcalculator.glass.GlassTheme
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class PreferencesRepository(
    private val store: DataStore<Preferences>,
) {
    val preferences: Flow<AppPreferences> = store.data.map(::decode)

    suspend fun setGlassIntensity(value: Float) = store.edit {
        it[Keys.glassIntensity] = value.coerceIn(0.25f, 1f)
    }

    suspend fun setMotionEffects(enabled: Boolean) = store.edit {
        it[Keys.motionEffects] = enabled
    }

    suspend fun setHaptics(enabled: Boolean) = store.edit {
        it[Keys.haptics] = enabled
    }

    suspend fun setQuality(quality: GlassQuality) = store.edit {
        it[Keys.quality] = quality.name
    }

    suspend fun setTheme(theme: GlassTheme) = store.edit {
        it[Keys.theme] = theme.name
    }

    suspend fun saveWallpaper(
        uri: String,
        zoom: Float,
        offsetX: Float,
        offsetY: Float,
    ) = store.edit {
        it[Keys.wallpaperUri] = uri
        it[Keys.wallpaperZoom] = zoom.coerceIn(1f, 5f)
        it[Keys.wallpaperOffsetX] = offsetX.coerceIn(-1f, 1f)
        it[Keys.wallpaperOffsetY] = offsetY.coerceIn(-1f, 1f)
    }

    suspend fun clearWallpaper() = store.edit {
        it.remove(Keys.wallpaperUri)
        it.remove(Keys.wallpaperZoom)
        it.remove(Keys.wallpaperOffsetX)
        it.remove(Keys.wallpaperOffsetY)
    }

    suspend fun resetAppearance() = store.edit {
        val haptics = it[Keys.haptics] ?: true
        it.clear()
        it[Keys.haptics] = haptics
    }

    companion object {
        internal object Keys {
            val glassIntensity = floatPreferencesKey("glass_intensity")
            val motionEffects = booleanPreferencesKey("motion_effects")
            val haptics = booleanPreferencesKey("haptics")
            val quality = stringPreferencesKey("glass_quality")
            val theme = stringPreferencesKey("glass_theme")
            val wallpaperUri = stringPreferencesKey("wallpaper_uri")
            val wallpaperZoom = floatPreferencesKey("wallpaper_zoom")
            val wallpaperOffsetX = floatPreferencesKey("wallpaper_offset_x")
            val wallpaperOffsetY = floatPreferencesKey("wallpaper_offset_y")
        }

        internal fun decode(values: Preferences): AppPreferences = AppPreferences(
            glassIntensity = (values[Keys.glassIntensity] ?: 0.72f).coerceIn(0.25f, 1f),
            motionEffects = values[Keys.motionEffects] ?: false,
            haptics = values[Keys.haptics] ?: true,
            quality = values[Keys.quality].toEnumOrDefault(GlassQuality.Adaptive),
            theme = values[Keys.theme].toEnumOrDefault(GlassTheme.Clear),
            wallpaperUri = values[Keys.wallpaperUri],
            wallpaperZoom = (values[Keys.wallpaperZoom] ?: 1f).coerceIn(1f, 5f),
            wallpaperOffsetX = (values[Keys.wallpaperOffsetX] ?: 0f).coerceIn(-1f, 1f),
            wallpaperOffsetY = (values[Keys.wallpaperOffsetY] ?: 0f).coerceIn(-1f, 1f),
        )

        private inline fun <reified T : Enum<T>> String?.toEnumOrDefault(default: T): T =
            this?.let { value -> enumValues<T>().firstOrNull { it.name == value } } ?: default
    }
}

