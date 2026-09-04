package com.kaizenflims.liquidcalculator.ui

import android.os.Build
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kaizenflims.liquidcalculator.data.AppPreferences
import com.kaizenflims.liquidcalculator.glass.GlassPalette
import com.kaizenflims.liquidcalculator.glass.GlassQuality
import com.kaizenflims.liquidcalculator.glass.GlassTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsSheet(
    preferences: AppPreferences,
    hasCustomWallpaper: Boolean,
    palette: GlassPalette,
    onDismiss: () -> Unit,
    onChooseWallpaper: () -> Unit,
    onRemoveWallpaper: () -> Unit,
    onGlassIntensity: (Float) -> Unit,
    onMotionEffects: (Boolean) -> Unit,
    onHaptics: (Boolean) -> Unit,
    onQuality: (GlassQuality) -> Unit,
    onTheme: (GlassTheme) -> Unit,
    onResetAppearance: () -> Unit,
) {
    val container = if (palette.label.red < 0.5f) {
        Color(0xF2F0F4F7)
    } else {
        Color(0xF2111A26)
    }
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = container,
        contentColor = palette.label,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 22.dp)
                .padding(bottom = 38.dp),
        ) {
            Text(
                text = "Appearance",
                color = palette.label,
                fontSize = 27.sp,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = "Tune the optical material, not just its color.",
                color = palette.secondaryLabel,
                fontSize = 13.sp,
            )

            SectionTitle("Wallpaper")
            Button(
                onClick = onChooseWallpaper,
                colors = ButtonDefaults.buttonColors(
                    containerColor = palette.operatorTint.copy(alpha = 0.34f),
                    contentColor = palette.label,
                ),
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(if (hasCustomWallpaper) "Choose another image" else "Choose an image")
            }
            if (hasCustomWallpaper) {
                TextButton(
                    onClick = onRemoveWallpaper,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("Restore original wallpaper", color = palette.secondaryLabel)
                }
            }

            SectionTitle("Glass intensity")
            Slider(
                value = preferences.glassIntensity,
                onValueChange = onGlassIntensity,
                valueRange = 0.25f..1f,
                steps = 6,
            )

            SectionTitle("Material tint")
            GlassTheme.entries.forEach { theme ->
                ChoiceRow(
                    title = theme.displayName,
                    selected = preferences.theme == theme,
                    palette = palette,
                    onClick = { onTheme(theme) },
                )
            }

            SectionTitle("Rendering quality")
            GlassQuality.entries.forEach { quality ->
                ChoiceRow(
                    title = quality.displayName,
                    subtitle = qualityDescription(quality),
                    selected = preferences.quality == quality,
                    palette = palette,
                    onClick = { onQuality(quality) },
                )
            }
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
                Text(
                    text = "This Android version uses the compatibility material. Full refractive AGSL requires Android 13 or newer.",
                    color = palette.secondaryLabel,
                    fontSize = 12.sp,
                    lineHeight = 17.sp,
                    modifier = Modifier.padding(top = 5.dp),
                )
            }

            SectionTitle("Interaction")
            ToggleRow(
                title = "Motion depth",
                subtitle = "Subtle sensor-based wallpaper parallax",
                checked = preferences.motionEffects,
                palette = palette,
                onChecked = onMotionEffects,
            )
            ToggleRow(
                title = "Haptics",
                subtitle = "Light, system-respecting tactile feedback",
                checked = preferences.haptics,
                palette = palette,
                onChecked = onHaptics,
            )

            Spacer(Modifier.height(22.dp))
            TextButton(
                onClick = onResetAppearance,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Reset appearance", color = palette.secondaryLabel)
            }
        }
    }
}

@Composable
private fun SectionTitle(title: String) {
    Text(
        text = title,
        fontSize = 13.sp,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.padding(top = 24.dp, bottom = 8.dp),
    )
}

@Composable
private fun ChoiceRow(
    title: String,
    selected: Boolean,
    palette: GlassPalette,
    onClick: () -> Unit,
    subtitle: String? = null,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RadioButton(selected = selected, onClick = onClick)
        Column(Modifier.padding(start = 6.dp)) {
            Text(title, color = palette.label, fontSize = 15.sp)
            if (subtitle != null) {
                Text(
                    subtitle,
                    color = palette.secondaryLabel,
                    fontSize = 12.sp,
                    lineHeight = 15.sp,
                )
            }
        }
    }
}

@Composable
private fun ToggleRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    palette: GlassPalette,
    onChecked: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onChecked(!checked) }
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Column(Modifier.weight(1f)) {
            Text(title, color = palette.label, fontSize = 15.sp)
            Text(subtitle, color = palette.secondaryLabel, fontSize = 12.sp)
        }
        Switch(checked = checked, onCheckedChange = onChecked)
    }
}

private fun qualityDescription(quality: GlassQuality): String = when (quality) {
    GlassQuality.Adaptive -> "Chooses the best renderer for this device"
    GlassQuality.High -> "Full refraction, dispersion and touch deformation"
    GlassQuality.Balanced -> "Reduced shader complexity and dispersion"
    GlassQuality.Compatibility -> "Layered translucent fallback"
}
