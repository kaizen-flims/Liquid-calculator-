package com.kaizenflims.liquidcalculator.wallpaper

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.unit.IntSize
import kotlin.math.max

data class WallpaperTransform(
    val zoom: Float = 1f,
    val offsetX: Float = 0f,
    val offsetY: Float = 0f,
) {
    fun sanitized(): WallpaperTransform = copy(
        zoom = zoom.coerceIn(1f, 5f),
        offsetX = offsetX.coerceIn(-1f, 1f),
        offsetY = offsetY.coerceIn(-1f, 1f),
    )
}

data class WallpaperAsset(
    val bitmap: ImageBitmap,
    val sourceUri: String?,
    val averageLuminance: Float,
    val contrast: Float,
    val luminanceMap: LuminanceMap,
)

data class LuminanceMap(
    val columns: Int,
    val rows: Int,
    val values: List<Float>,
) {
    fun sample(u: Float, v: Float): Float {
        if (values.isEmpty() || columns <= 0 || rows <= 0) return 0.5f
        val x = (u.coerceIn(0f, 0.9999f) * columns).toInt().coerceIn(0, columns - 1)
        val y = (v.coerceIn(0f, 0.9999f) * rows).toInt().coerceIn(0, rows - 1)
        return values[y * columns + x]
    }
}

data class WallpaperFrame(
    val asset: WallpaperAsset,
    val transform: WallpaperTransform,
)

data class WallpaperPlacement(
    val topLeft: Offset,
    val size: Size,
    val maxTravel: Offset,
)

fun calculateWallpaperPlacement(
    rootSize: IntSize,
    imageSize: IntSize,
    transform: WallpaperTransform,
    parallax: Offset = Offset.Zero,
): WallpaperPlacement {
    if (rootSize.width <= 0 || rootSize.height <= 0 || imageSize.width <= 0 || imageSize.height <= 0) {
        return WallpaperPlacement(Offset.Zero, Size.Zero, Offset.Zero)
    }
    val clean = transform.sanitized()
    val baseScale = max(
        rootSize.width.toFloat() / imageSize.width,
        rootSize.height.toFloat() / imageSize.height,
    )
    val scale = baseScale * clean.zoom
    val width = imageSize.width * scale
    val height = imageSize.height * scale
    val maxX = ((width - rootSize.width) / 2f).coerceAtLeast(0f)
    val maxY = ((height - rootSize.height) / 2f).coerceAtLeast(0f)
    val x = (rootSize.width - width) / 2f + (clean.offsetX * maxX) + parallax.x
    val y = (rootSize.height - height) / 2f + (clean.offsetY * maxY) + parallax.y
    return WallpaperPlacement(
        topLeft = Offset(x, y),
        size = Size(width, height),
        maxTravel = Offset(maxX, maxY),
    )
}

fun WallpaperFrame.luminanceAt(
    pointInRoot: Offset,
    rootSize: IntSize,
    parallax: Offset = Offset.Zero,
): Float {
    val placement = calculateWallpaperPlacement(
        rootSize = rootSize,
        imageSize = IntSize(asset.bitmap.width, asset.bitmap.height),
        transform = transform,
        parallax = parallax,
    )
    if (placement.size.width <= 0f || placement.size.height <= 0f) {
        return asset.averageLuminance
    }
    val u = (pointInRoot.x - placement.topLeft.x) / placement.size.width
    val v = (pointInRoot.y - placement.topLeft.y) / placement.size.height
    return asset.luminanceMap.sample(u, v)
}
