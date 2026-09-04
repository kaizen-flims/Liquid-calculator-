package com.kaizenflims.liquidcalculator.wallpaper

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.max

class WallpaperRepository(
    private val context: Context,
) {
    val defaultWallpaper: WallpaperAsset by lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
        DefaultWallpaperFactory.create()
    }

    suspend fun load(uriString: String): WallpaperAsset? = withContext(Dispatchers.IO) {
        runCatching {
            val uri = Uri.parse(uriString)
            val bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                decodeModern(uri)
            } else {
                decodeLegacy(uri)
            }
            WallpaperAnalyzer.analyze(bitmap, uriString)
        }.getOrNull()
    }

    fun persistReadPermission(uri: Uri) {
        runCatching {
            context.contentResolver.takePersistableUriPermission(
                uri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION,
            )
        }
    }

    @androidx.annotation.RequiresApi(Build.VERSION_CODES.P)
    private fun decodeModern(uri: Uri): Bitmap {
        val source = ImageDecoder.createSource(context.contentResolver, uri)
        return ImageDecoder.decodeBitmap(source) { decoder, info, _ ->
            val width = info.size.width
            val height = info.size.height
            val largest = max(width, height)
            if (largest > MAX_TEXTURE_EDGE) {
                val scale = MAX_TEXTURE_EDGE.toFloat() / largest
                decoder.setTargetSize((width * scale).toInt(), (height * scale).toInt())
            }
            decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
        }
    }

    private fun decodeLegacy(uri: Uri): Bitmap {
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        context.contentResolver.openInputStream(uri).use {
            BitmapFactory.decodeStream(it, null, bounds)
        }
        var sample = 1
        while (max(bounds.outWidth, bounds.outHeight) / sample > MAX_TEXTURE_EDGE) sample *= 2
        val options = BitmapFactory.Options().apply {
            inSampleSize = sample
            inPreferredConfig = Bitmap.Config.ARGB_8888
        }
        return requireNotNull(
            context.contentResolver.openInputStream(uri).use {
                BitmapFactory.decodeStream(it, null, options)
            },
        )
    }

    private companion object {
        const val MAX_TEXTURE_EDGE = 3072
    }
}

