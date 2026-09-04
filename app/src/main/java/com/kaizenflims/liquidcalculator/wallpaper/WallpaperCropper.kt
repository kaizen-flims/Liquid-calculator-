package com.kaizenflims.liquidcalculator.wallpaper

import android.content.res.Configuration
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kaizenflims.liquidcalculator.calculator.CalculatorState
import com.kaizenflims.liquidcalculator.glass.GlassButtonKind
import com.kaizenflims.liquidcalculator.glass.GlassQuality
import com.kaizenflims.liquidcalculator.glass.GlassTheme
import com.kaizenflims.liquidcalculator.glass.glassPalette
import com.kaizenflims.liquidcalculator.ui.CalculatorKeypad
import com.kaizenflims.liquidcalculator.ui.DisplayPanel
import com.kaizenflims.liquidcalculator.utils.performCalculatorHaptic

@Composable
fun WallpaperCropper(
    asset: WallpaperAsset,
    transform: WallpaperTransform,
    intensity: Float,
    quality: GlassQuality,
    theme: GlassTheme,
    haptics: Boolean,
    onTransform: (WallpaperTransform) -> Unit,
    onCancel: () -> Unit,
    onApply: () -> Unit,
) {
    var rootSize by remember { mutableStateOf(IntSize.Zero) }
    val currentTransform by rememberUpdatedState(transform)
    val currentOnTransform by rememberUpdatedState(onTransform)
    val isLandscape = LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE
    val frame = WallpaperFrame(asset, transform)
    val palette = glassPalette(asset, theme, intensity)
    val view = LocalView.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .onSizeChanged { rootSize = it }
            .pointerInput(rootSize, asset) {
                detectTransformGestures { _, pan, gestureZoom, _ ->
                    if (rootSize == IntSize.Zero) return@detectTransformGestures
                    val old = currentTransform.sanitized()
                    val oldPlacement = calculateWallpaperPlacement(
                        rootSize,
                        IntSize(asset.bitmap.width, asset.bitmap.height),
                        old,
                    )
                    val newZoom = (old.zoom * gestureZoom).coerceIn(1f, 5f)
                    val zoomed = old.copy(zoom = newZoom)
                    val newPlacement = calculateWallpaperPlacement(
                        rootSize,
                        IntSize(asset.bitmap.width, asset.bitmap.height),
                        zoomed,
                    )
                    val xPixels = old.offsetX * oldPlacement.maxTravel.x + pan.x
                    val yPixels = old.offsetY * oldPlacement.maxTravel.y + pan.y
                    currentOnTransform(
                        zoomed.copy(
                            offsetX = if (newPlacement.maxTravel.x > 0f) {
                                (xPixels / newPlacement.maxTravel.x).coerceIn(-1f, 1f)
                            } else {
                                0f
                            },
                            offsetY = if (newPlacement.maxTravel.y > 0f) {
                                (yPixels / newPlacement.maxTravel.y).coerceIn(-1f, 1f)
                            } else {
                                0f
                            },
                        ),
                    )
                }
            },
    ) {
        Canvas(Modifier.fillMaxSize()) {
            drawWallpaper(frame, rootSize)
            drawRect(
                Brush.verticalGradient(
                    listOf(
                        Color.Black.copy(alpha = 0.08f),
                        Color.Transparent,
                        Color.Black.copy(alpha = 0.13f),
                    ),
                ),
            )
        }

        if (isLandscape) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .systemBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(18.dp),
            ) {
                Column(
                    modifier = Modifier
                        .weight(0.34f)
                        .fillMaxHeight(),
                ) {
                    CropToolbar(
                        palette.label,
                        palette.bodyTint,
                        onCancel,
                        { onTransform(WallpaperTransform()) },
                        onApply,
                    )
                    DisplayPanel(
                        state = CalculatorState(display = "128.4", expression = "64.2 × 2"),
                        palette = palette,
                        isLandscape = true,
                        onSwipeDown = {},
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                    )
                    Text(
                        text = "Pinch to zoom · drag to position",
                        color = palette.secondaryLabel,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(bottom = 5.dp),
                    )
                }
                CalculatorKeypad(
                    isLandscape = true,
                    wallpaper = frame,
                    rootSize = rootSize,
                    palette = palette,
                    intensity = intensity,
                    quality = quality,
                    parallax = Offset.Zero,
                    enabled = false,
                    onHaptic = {},
                    onAction = {},
                    modifier = Modifier
                        .weight(0.66f)
                        .fillMaxHeight(),
                )
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .systemBarsPadding()
                    .padding(horizontal = 18.dp, vertical = 10.dp),
            ) {
                CropToolbar(
                    palette.label,
                    palette.bodyTint,
                    onCancel,
                    { onTransform(WallpaperTransform()) },
                    onApply,
                )
                Text(
                    text = "LIVE GLASS PREVIEW",
                    color = palette.secondaryLabel,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 1.5.sp,
                    modifier = Modifier.padding(top = 10.dp),
                )
                DisplayPanel(
                    state = CalculatorState(display = "128.4", expression = "64.2 × 2"),
                    palette = palette,
                    isLandscape = false,
                    onSwipeDown = {},
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(0.31f),
                )
                CalculatorKeypad(
                    isLandscape = false,
                    wallpaper = frame,
                    rootSize = rootSize,
                    palette = palette,
                    intensity = intensity,
                    quality = quality,
                    parallax = Offset.Zero,
                    enabled = false,
                    onHaptic = {},
                    onAction = {},
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(0.69f),
                )
                Text(
                    text = "Pinch to zoom · drag to position",
                    color = palette.secondaryLabel,
                    fontSize = 12.sp,
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(top = 8.dp),
                )
            }
        }
    }
}

@Composable
private fun CropToolbar(
    contentColor: Color,
    containerColor: Color,
    onCancel: () -> Unit,
    onReset: () -> Unit,
    onApply: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(44.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CropActionButton("Cancel", contentColor, containerColor, onCancel)
        CropActionButton("Reset", contentColor, containerColor, onReset)
        CropActionButton("Apply", contentColor, containerColor.copy(alpha = 0.32f), onApply)
    }
}

@Composable
private fun CropActionButton(
    label: String,
    contentColor: Color,
    containerColor: Color,
    onClick: () -> Unit,
) {
    Surface(
        color = containerColor.copy(alpha = containerColor.alpha.coerceAtLeast(0.16f)),
        shape = RoundedCornerShape(50),
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(horizontal = 1.dp),
    ) {
        Box(
            modifier = Modifier.padding(horizontal = 17.dp, vertical = 9.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = label,
                color = contentColor,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
            )
        }
    }
}
