package com.kaizenflims.liquidcalculator.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LiquidColors = darkColorScheme(
    primary = Color(0xFFB6E9FA),
    onPrimary = Color(0xFF0A2530),
    surface = Color(0xFF121A26),
    onSurface = Color(0xFFF5FAFF),
    surfaceVariant = Color(0xFF263140),
    onSurfaceVariant = Color(0xFFD5E1EC),
)

@Composable
fun LiquidCalculatorTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LiquidColors,
        typography = MaterialTheme.typography,
        content = content,
    )
}

