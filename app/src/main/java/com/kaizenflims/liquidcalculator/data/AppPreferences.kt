package com.kaizenflims.liquidcalculator.data

import com.kaizenflims.liquidcalculator.glass.GlassQuality
import com.kaizenflims.liquidcalculator.glass.GlassTheme

data class AppPreferences(
    val glassIntensity: Float = 0.72f,
    val motionEffects: Boolean = false,
    val haptics: Boolean = true,
    val quality: GlassQuality = GlassQuality.Adaptive,
    val theme: GlassTheme = GlassTheme.Clear,
    val wallpaperUri: String? = null,
    val wallpaperZoom: Float = 1f,
    val wallpaperOffsetX: Float = 0f,
    val wallpaperOffsetY: Float = 0f,
)

