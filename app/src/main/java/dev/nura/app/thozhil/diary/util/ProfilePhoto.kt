package dev.nura.app.thozhil.diary.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

/** Profile photos are kept small: plenty for a 120dp avatar on a tablet. */
private const val PHOTO_MAX_SIDE = 512

/**
 * Copies the picked photo into the app's own storage, scaled down, and returns its path. Each save
 * gets a new name so a changed photo is never mistaken for the old one; [previousPath] is removed.
 */
suspend fun saveProfilePhoto(context: Context, uri: Uri, previousPath: String): String? = withContext(Dispatchers.IO) {
    runCatching {
        val bitmap = decodeScaled(context, uri) ?: return@runCatching null
        val dir = File(context.filesDir, "profile").apply { mkdirs() }
        val file = File(dir, "photo_${System.currentTimeMillis()}.jpg")
        file.outputStream().use { bitmap.compress(Bitmap.CompressFormat.JPEG, 88, it) }
        deleteProfilePhoto(previousPath)
        file.absolutePath
    }.getOrNull()
}

fun deleteProfilePhoto(path: String) {
    if (path.isNotBlank()) runCatching { File(path).delete() }
}

/** The saved photo, or null when there is none or it can't be read. */
suspend fun loadProfilePhoto(path: String): Bitmap? = withContext(Dispatchers.IO) {
    if (path.isBlank() || !File(path).exists()) null
    else runCatching { BitmapFactory.decodeFile(path) }.getOrNull()
}

private fun decodeScaled(context: Context, uri: Uri): Bitmap? {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        val source = ImageDecoder.createSource(context.contentResolver, uri)
        // ImageDecoder also turns the photo the right way up
        return ImageDecoder.decodeBitmap(source) { decoder, info, _ ->
            val longest = maxOf(info.size.width, info.size.height)
            if (longest > PHOTO_MAX_SIDE) {
                val scale = PHOTO_MAX_SIDE.toFloat() / longest
                decoder.setTargetSize((info.size.width * scale).toInt(), (info.size.height * scale).toInt())
            }
            decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
        }
    }
    val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    context.contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, bounds) }
    var sample = 1
    while (maxOf(bounds.outWidth, bounds.outHeight) / (sample * 2) >= PHOTO_MAX_SIDE) sample *= 2
    return context.contentResolver.openInputStream(uri)?.use {
        BitmapFactory.decodeStream(it, null, BitmapFactory.Options().apply { inSampleSize = sample })
    }
}
