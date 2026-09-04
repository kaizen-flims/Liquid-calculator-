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

internal object PreferenceKeys {
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

class PreferencesRepository(
    private val store: DataStore<Preferences>,
) {
    val preferences: Flow<AppPreferences> = store.data.map(::decode)

    suspend fun setGlassIntensity(value: Float) = store.edit {
        it[PreferenceKeys.glassIntensity] = value.coerceIn(0.25f, 1f)
    }

    suspend fun setMotionEffects(enabled: Boolean) = store.edit {
        it[PreferenceKeys.motionEffects] = enabled
    }

    suspend fun setHaptics(enabled: Boolean) = store.edit {
        it[PreferenceKeys.haptics] = enabled
    }

    suspend fun setQuality(quality: GlassQuality) = store.edit {
        it[PreferenceKeys.quality] = quality.name
    }

    suspend fun setTheme(theme: GlassTheme) = store.edit {
        it[PreferenceKeys.theme] = theme.name
    }

    suspend fun saveWallpaper(
        uri: String,
        zoom: Float,
        offsetX: Float,
        offsetY: Float,
    ) = store.edit {
        it[PreferenceKeys.wallpaperUri] = uri
        it[PreferenceKeys.wallpaperZoom] = zoom.coerceIn(1f, 5f)
        it[PreferenceKeys.wallpaperOffsetX] = offsetX.coerceIn(-1f, 1f)
        it[PreferenceKeys.wallpaperOffsetY] = offsetY.coerceIn(-1f, 1f)
    }

    suspend fun clearWallpaper() = store.edit {
        it.remove(PreferenceKeys.wallpaperUri)
        it.remove(PreferenceKeys.wallpaperZoom)
        it.remove(PreferenceKeys.wallpaperOffsetX)
        it.remove(PreferenceKeys.wallpaperOffsetY)
    }

    suspend fun resetAppearance() = store.edit {
        val haptics = it[PreferenceKeys.haptics] ?: true
        it.clear()
        it[PreferenceKeys.haptics] = haptics
    }

    companion object {
        internal fun decode(values: Preferences): AppPreferences = AppPreferences(
            glassIntensity = (values[PreferenceKeys.glassIntensity] ?: 0.72f).coerceIn(0.25f, 1f),
            motionEffects = values[PreferenceKeys.motionEffects] ?: false,
            haptics = values[PreferenceKeys.haptics] ?: true,
            quality = values[PreferenceKeys.quality].toEnumOrDefault(GlassQuality.Adaptive),
            theme = values[PreferenceKeys.theme].toEnumOrDefault(GlassTheme.Clear),
            wallpaperUri = values[PreferenceKeys.wallpaperUri],
            wallpaperZoom = (values[PreferenceKeys.wallpaperZoom] ?: 1f).coerceIn(1f, 5f),
            wallpaperOffsetX = (values[PreferenceKeys.wallpaperOffsetX] ?: 0f).coerceIn(-1f, 1f),
            wallpaperOffsetY = (values[PreferenceKeys.wallpaperOffsetY] ?: 0f).coerceIn(-1f, 1f),
        )

        private inline fun <reified T : Enum<T>> String?.toEnumOrDefault(default: T): T =
            this?.let { value -> enumValues<T>().firstOrNull { it.name == value } } ?: default
    }
}
