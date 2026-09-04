package com.kaizenflims.liquidcalculator.wallpaper

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.RadialGradient
import android.graphics.Shader
import androidx.compose.ui.graphics.asImageBitmap

object DefaultWallpaperFactory {
    private const val WIDTH = 1080
    private const val HEIGHT = 2160

    fun create(): WallpaperAsset {
        val bitmap = Bitmap.createBitmap(WIDTH, HEIGHT, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        paint.shader = LinearGradient(
            0f,
            0f,
            WIDTH.toFloat(),
            HEIGHT.toFloat(),
            intArrayOf(
                Color.rgb(7, 18, 39),
                Color.rgb(24, 39, 74),
                Color.rgb(36, 19, 64),
                Color.rgb(9, 30, 47),
            ),
            floatArrayOf(0f, 0.32f, 0.68f, 1f),
            Shader.TileMode.CLAMP,
        )
        canvas.drawRect(0f, 0f, WIDTH.toFloat(), HEIGHT.toFloat(), paint)

        drawOrb(canvas, -80f, 390f, 650f, Color.rgb(30, 170, 211), 0.72f)
        drawOrb(canvas, 970f, 690f, 720f, Color.rgb(117, 74, 212), 0.62f)
        drawOrb(canvas, 230f, 1870f, 760f, Color.rgb(19, 107, 145), 0.55f)
        drawOrb(canvas, 1080f, 1740f, 560f, Color.rgb(222, 101, 115), 0.34f)

        paint.shader = LinearGradient(
            0f,
            700f,
            WIDTH.toFloat(),
            1460f,
            intArrayOf(Color.TRANSPARENT, Color.argb(72, 214, 240, 255), Color.TRANSPARENT),
            floatArrayOf(0f, 0.48f, 1f),
            Shader.TileMode.CLAMP,
        )
        canvas.save()
        canvas.rotate(-18f, WIDTH / 2f, HEIGHT / 2f)
        canvas.drawRoundRect(-240f, 920f, 1350f, 1090f, 90f, 90f, paint)
        canvas.restore()

        return WallpaperAnalyzer.analyze(bitmap, sourceUri = null)
    }

    private fun drawOrb(
        canvas: Canvas,
        x: Float,
        y: Float,
        radius: Float,
        color: Int,
        opacity: Float,
    ) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = RadialGradient(
                x,
                y,
                radius,
                intArrayOf(
                    Color.argb((255 * opacity).toInt(), Color.red(color), Color.green(color), Color.blue(color)),
                    Color.argb((90 * opacity).toInt(), Color.red(color), Color.green(color), Color.blue(color)),
                    Color.TRANSPARENT,
                ),
                floatArrayOf(0f, 0.48f, 1f),
                Shader.TileMode.CLAMP,
            )
        }
        canvas.drawCircle(x, y, radius, paint)
    }
}

object WallpaperAnalyzer {
    fun analyze(bitmap: Bitmap, sourceUri: String?): WallpaperAsset {
        val columns = 12
        val rows = 20
        val localValues = ArrayList<Float>(columns * rows)
        var luminanceTotal = 0.0
        var luminanceSquared = 0.0
        var samples = 0
        for (row in 0 until rows) {
            for (column in 0 until columns) {
                val x = (((column + 0.5f) / columns) * bitmap.width)
                    .toInt()
                    .coerceIn(0, bitmap.width - 1)
                val y = (((row + 0.5f) / rows) * bitmap.height)
                    .toInt()
                    .coerceIn(0, bitmap.height - 1)
                val color = bitmap.getPixel(x, y)
                val r = Color.red(color) / 255.0
                val g = Color.green(color) / 255.0
                val b = Color.blue(color) / 255.0
                val luminance = 0.2126 * r + 0.7152 * g + 0.0722 * b
                localValues += luminance.toFloat()
                luminanceTotal += luminance
                luminanceSquared += luminance * luminance
                samples++
            }
        }
        val average = if (samples == 0) 0.5 else luminanceTotal / samples
        val variance = if (samples == 0) 0.0 else luminanceSquared / samples - average * average
        return WallpaperAsset(
            bitmap = bitmap.asImageBitmap(),
            sourceUri = sourceUri,
            averageLuminance = average.toFloat(),
            contrast = kotlin.math.sqrt(variance.coerceAtLeast(0.0)).toFloat(),
            luminanceMap = LuminanceMap(columns, rows, localValues),
        )
    }
}
