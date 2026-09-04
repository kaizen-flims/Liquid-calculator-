package com.kaizenflims.liquidcalculator.wallpaper

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import kotlin.math.roundToInt

fun DrawScope.drawWallpaper(
    frame: WallpaperFrame,
    rootSize: IntSize,
    originInRoot: Offset = Offset.Zero,
    parallax: Offset = Offset.Zero,
) {
    val image = frame.asset.bitmap
    val placement = calculateWallpaperPlacement(
        rootSize = rootSize,
        imageSize = IntSize(image.width, image.height),
        transform = frame.transform,
        parallax = parallax,
    )
    if (placement.size.width <= 0f || placement.size.height <= 0f) return

    drawImage(
        image = image,
        dstOffset = IntOffset(
            (placement.topLeft.x - originInRoot.x).roundToInt(),
            (placement.topLeft.y - originInRoot.y).roundToInt(),
        ),
        dstSize = IntSize(
            placement.size.width.roundToInt(),
            placement.size.height.roundToInt(),
        ),
        filterQuality = FilterQuality.High,
    )
}

