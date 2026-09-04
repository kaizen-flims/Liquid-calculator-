package com.kaizenflims.liquidcalculator.glass

import android.graphics.RuntimeShader
import android.os.Build
import android.app.ActivityManager
import android.content.Context
import androidx.annotation.RequiresApi
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.matchParentSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.disabled
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kaizenflims.liquidcalculator.R
import com.kaizenflims.liquidcalculator.wallpaper.WallpaperFrame
import com.kaizenflims.liquidcalculator.wallpaper.drawWallpaper
import com.kaizenflims.liquidcalculator.wallpaper.luminanceAt
import kotlinx.coroutines.launch
import kotlin.math.min

enum class GlassButtonKind {
    Number,
    Utility,
    Operator,
    Equals,
    Scientific,
}

@Composable
fun LiquidGlassButton(
    label: String,
    kind: GlassButtonKind,
    wallpaper: WallpaperFrame,
    rootSize: IntSize,
    palette: GlassPalette,
    intensity: Float,
    quality: GlassQuality,
    parallax: Offset,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    contentDescription: String = label,
    onHaptic: (GlassButtonKind) -> Unit = {},
    onClick: () -> Unit,
) {
    val context = LocalContext.current
    val view = LocalView.current
    val isLowRamDevice = remember {
        (context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager).isLowRamDevice
    }
    val shaderSource = remember {
        context.resources.openRawResource(R.raw.liquid_lens)
            .bufferedReader()
            .use { it.readText() }
    }
    val resolvedQuality = resolveQuality(
        requested = quality,
        isLowRamDevice = isLowRamDevice,
        hardwareAccelerated = view.isHardwareAccelerated,
    )
    val shader = rememberLiquidShader(shaderSource, resolvedQuality)
    val press = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()
    var touch by remember { mutableStateOf(Offset.Zero) }
    var origin by remember { mutableStateOf(Offset.Zero) }
    var localSize by remember { mutableStateOf(IntSize.Zero) }
    val shape = RoundedCornerShape(percent = 50)
    val baseTint = when (kind) {
        GlassButtonKind.Number, GlassButtonKind.Scientific -> palette.bodyTint
        GlassButtonKind.Utility -> palette.utilityTint
        GlassButtonKind.Operator -> palette.operatorTint
        GlassButtonKind.Equals -> palette.equalsTint
    }
    val localLuminance = if (localSize != IntSize.Zero) {
        wallpaper.luminanceAt(
            pointInRoot = origin + Offset(localSize.width / 2f, localSize.height / 2f),
            rootSize = rootSize,
            parallax = parallax,
        )
    } else {
        wallpaper.asset.averageLuminance
    }
    val textColor = if (localLuminance > 0.62f) Color(0xFF09121D) else Color(0xFFF8FCFF)
    val tint = when {
        localLuminance > 0.66f -> lerp(baseTint, Color(0xFF071522).copy(alpha = 0.22f), 0.46f)
        localLuminance < 0.28f -> lerp(baseTint, Color.White.copy(alpha = 0.15f), 0.28f)
        else -> baseTint
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .graphicsLayer {
                val pressure = press.value
                scaleX = 1f + pressure * 0.004f
                scaleY = 1f - pressure * 0.018f
                rotationX = if (localSize.height > 0) {
                    ((touch.y / localSize.height) - 0.5f) * pressure * -1.1f
                } else {
                    0f
                }
                rotationY = if (localSize.width > 0) {
                    ((touch.x / localSize.width) - 0.5f) * pressure * 1.1f
                } else {
                    0f
                }
                shadowElevation = (10f - pressure * 5f).coerceAtLeast(2f)
                this.shape = shape
                clip = false
            }
            .onGloballyPositioned {
                origin = it.positionInRoot()
                localSize = it.size
                if (touch == Offset.Zero) {
                    touch = Offset(it.size.width / 2f, it.size.height / 2f)
                }
            }
            .semantics(mergeDescendants = true) {
                role = Role.Button
                this.contentDescription = contentDescription
                if (!enabled) disabled()
                onClick {
                    if (enabled) {
                        onHaptic(kind)
                        onClick()
                    }
                    enabled
                }
            }
            .then(
                if (enabled) {
                    Modifier.pointerInput(label, enabled) {
                        awaitEachGesture {
                            val down = awaitFirstDown(requireUnconsumed = false)
                            down.consume()
                            touch = down.position
                            onHaptic(kind)
                            scope.launch {
                                press.stop()
                                press.animateTo(1f, tween(durationMillis = 54))
                            }
                            val up = waitForUpOrCancellation()
                            if (up != null) {
                                up.consume()
                                onClick()
                            }
                            scope.launch {
                                press.stop()
                                press.animateTo(
                                    targetValue = 0f,
                                    animationSpec = spring(
                                        dampingRatio = 0.82f,
                                        stiffness = Spring.StiffnessHigh,
                                    ),
                                )
                            }
                        }
                    }
                } else {
                    Modifier
                },
            ),
    ) {
        Box(
            Modifier
                .matchParentSize()
                .graphicsLayer {
                    this.shape = shape
                    clip = true
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && shader != null) {
                        shader.setFloatUniform("size", size.width, size.height)
                        shader.setFloatUniform(
                            "touch",
                            touch.x.coerceIn(0f, size.width),
                            touch.y.coerceIn(0f, size.height),
                        )
                        shader.setFloatUniform("press", press.value)
                        shader.setFloatUniform(
                            "intensity",
                            intensity * if (resolvedQuality == GlassQuality.High) 1f else 0.72f,
                        )
                        shader.setFloatUniform("corner", min(size.width, size.height) / 2f)
                        shader.setFloatUniform(
                            "dispersion",
                            if (resolvedQuality == GlassQuality.High) 0.9f else 0.32f,
                        )
                        renderEffect = runtimeShaderEffect(shader)
                    } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        renderEffect = compatibilityBlurEffect()
                    }
                }
                .drawWithCache {
                    onDrawBehind {
                        drawWallpaper(
                            frame = wallpaper,
                            rootSize = rootSize,
                            originInRoot = origin,
                            parallax = parallax,
                        )
                        drawRect(tint)
                        drawRect(
                            Brush.radialGradient(
                                colors = listOf(
                                    Color.White.copy(alpha = 0.10f + press.value * 0.05f),
                                    Color.Transparent,
                                ),
                                center = touch,
                                radius = size.minDimension * (0.65f + press.value * 0.08f),
                            ),
                        )
                    }
                },
        )

        GlassDepthOverlay(
            palette = palette,
            tint = tint,
            press = press.value,
            touch = touch,
            shape = shape,
        )

        Text(
            text = label,
            color = textColor,
            fontSize = when (kind) {
                GlassButtonKind.Scientific -> 17.sp
                GlassButtonKind.Utility -> 23.sp
                else -> 28.sp
            },
            lineHeight = 30.sp,
            fontWeight = when (kind) {
                GlassButtonKind.Equals -> FontWeight.SemiBold
                GlassButtonKind.Operator -> FontWeight.Medium
                else -> FontWeight.Normal
            },
            modifier = Modifier.padding(horizontal = 8.dp),
        )
    }
}

@Composable
private fun GlassDepthOverlay(
    palette: GlassPalette,
    tint: Color,
    press: Float,
    touch: Offset,
    shape: RoundedCornerShape,
) {
    Canvas(
        Modifier
            .matchParentSize()
            .clip(shape),
    ) {
        val radius = size.minDimension / 2f
        val inset = 1.25.dp.toPx()
        val corner = (radius - inset).coerceAtLeast(0f)
        drawRoundRect(
            brush = Brush.linearGradient(
                colors = listOf(
                    palette.rimLight.copy(alpha = palette.rimLight.alpha * (1f - press * 0.22f)),
                    Color.Transparent,
                    palette.rimShade.copy(alpha = palette.rimShade.alpha + press * 0.08f),
                ),
                start = Offset.Zero,
                end = Offset(size.width, size.height),
            ),
            topLeft = Offset(inset, inset),
            size = Size(size.width - inset * 2, size.height - inset * 2),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(corner, corner),
            style = Stroke(width = 1.25.dp.toPx()),
        )
        drawRoundRect(
            color = tint.copy(alpha = (0.08f + press * 0.07f).coerceAtMost(0.2f)),
            topLeft = Offset(3.dp.toPx(), 3.dp.toPx()),
            size = Size(size.width - 6.dp.toPx(), size.height - 6.dp.toPx()),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(radius - 3.dp.toPx()),
            style = Stroke(
                width = 0.7.dp.toPx(),
                pathEffect = PathEffect.cornerPathEffect(radius),
            ),
        )
        val specularCenter = if (touch == Offset.Zero) {
            Offset(size.width * 0.28f, size.height * 0.2f)
        } else {
            Offset(
                x = size.width * (
                    0.24f + (touch.x / size.width.coerceAtLeast(1f)) * 0.16f
                ),
                y = size.height * (
                    0.16f + (touch.y / size.height.coerceAtLeast(1f)) * 0.12f
                ),
            )
        }
        drawCircle(
            brush = Brush.radialGradient(
                listOf(
                    Color.White.copy(alpha = 0.20f + press * 0.08f),
                    Color.Transparent,
                ),
                center = specularCenter,
                radius = radius * 0.9f,
            ),
            radius = radius * 0.9f,
            center = specularCenter,
        )
    }
}

private fun resolveQuality(
    requested: GlassQuality,
    isLowRamDevice: Boolean,
    hardwareAccelerated: Boolean,
): GlassQuality = when {
    requested == GlassQuality.Compatibility -> GlassQuality.Compatibility
    Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU -> GlassQuality.Compatibility
    !hardwareAccelerated -> GlassQuality.Compatibility
    requested == GlassQuality.Adaptive && isLowRamDevice -> GlassQuality.Balanced
    requested == GlassQuality.Adaptive -> GlassQuality.High
    else -> requested
}

@Composable
private fun rememberLiquidShader(source: String, quality: GlassQuality): RuntimeShader? =
    if (
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
        quality != GlassQuality.Compatibility
    ) {
        remember(source) { runCatching { RuntimeShader(source) }.getOrNull() }
    } else {
        null
    }

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
private fun runtimeShaderEffect(shader: RuntimeShader) =
    android.graphics.RenderEffect.createRuntimeShaderEffect(shader, "content").asComposeRenderEffect()

@RequiresApi(Build.VERSION_CODES.S)
private fun compatibilityBlurEffect() =
    android.graphics.RenderEffect.createBlurEffect(
        2.4f,
        2.4f,
        android.graphics.Shader.TileMode.CLAMP,
    ).asComposeRenderEffect()
