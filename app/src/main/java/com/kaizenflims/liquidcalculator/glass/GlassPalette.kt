package com.kaizenflims.liquidcalculator.glass

import androidx.compose.ui.graphics.Color
import com.kaizenflims.liquidcalculator.wallpaper.WallpaperAsset

data class GlassPalette(
    val label: Color,
    val secondaryLabel: Color,
    val bodyTint: Color,
    val utilityTint: Color,
    val operatorTint: Color,
    val equalsTint: Color,
    val rimLight: Color,
    val rimShade: Color,
    val displayScrim: Color,
)

fun glassPalette(
    wallpaper: WallpaperAsset,
    theme: GlassTheme,
    intensity: Float,
): GlassPalette {
    val bright = wallpaper.averageLuminance > 0.61f
    val highContrast = wallpaper.contrast > 0.24f
    val label = if (bright) Color(0xFF0C1420) else Color(0xFFF7FBFF)
    val secondary = label.copy(alpha = if (bright) 0.68f else 0.72f)
    val adaptiveAlpha = ((if (bright) 0.19f else 0.09f) + if (highContrast) 0.045f else 0f) *
        intensity.coerceIn(0.25f, 1f)

    val base = when (theme) {
        GlassTheme.Clear -> if (bright) Color(0xFF101C2A) else Color.White
        GlassTheme.Graphite -> Color(0xFF202936)
        GlassTheme.Silver -> Color(0xFFD6E3EC)
        GlassTheme.Amber -> Color(0xFFF0A84E)
        GlassTheme.DeepBlue -> Color(0xFF4E7CCB)
    }
    val body = base.copy(alpha = adaptiveAlpha.coerceIn(0.055f, 0.28f))

    return GlassPalette(
        label = label,
        secondaryLabel = secondary,
        bodyTint = body,
        utilityTint = if (bright) Color(0xFF10263D).copy(alpha = 0.20f) else Color.White.copy(alpha = 0.15f),
        operatorTint = Color(0xFF66C7E8).copy(alpha = if (bright) 0.34f else 0.25f),
        equalsTint = Color(0xFFB4E8F7).copy(alpha = if (bright) 0.48f else 0.34f),
        rimLight = if (bright) Color.White.copy(alpha = 0.58f) else Color.White.copy(alpha = 0.72f),
        rimShade = if (bright) Color(0xFF07111D).copy(alpha = 0.42f) else Color.Black.copy(alpha = 0.36f),
        displayScrim = if (bright) Color.White.copy(alpha = 0.11f) else Color.Black.copy(alpha = 0.09f),
    )
}

