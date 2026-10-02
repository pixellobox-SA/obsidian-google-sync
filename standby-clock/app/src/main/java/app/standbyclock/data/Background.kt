package app.standbyclock.data

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import kotlin.math.max

/** What sits behind the clock. */
enum class BackgroundMode {
    /** The phone's own wallpaper, shown live by the system (no permission needed). */
    WALLPAPER,

    /** A photo the user picked, copied into the app's private storage. */
    PHOTO,

    /** Plain black, best for OLED screens at night. */
    BLACK,

    /** A single colour the user picks on a colour wheel, with its own brightness. */
    COLOR,
}

/** Stores and loads the user's chosen background photo. Never leaves the phone. */
object BackgroundPhoto {
    /** Longest side kept; plenty for a phone screen and keeps memory use low. */
    private const val MAX_SIDE = 2400

    private fun file(context: Context) = File(context.filesDir, "background.jpg")

    fun exists(context: Context) = file(context).exists()

    /** Copies a picked image into app storage, shrunk to a sensible size. */
    suspend fun save(context: Context, uri: Uri): Boolean = withContext(Dispatchers.IO) {
        runCatching {
            val bitmap = decode(context, uri) ?: return@runCatching false
            file(context).outputStream().use { bitmap.compress(Bitmap.CompressFormat.JPEG, 90, it) }
            bitmap.recycle()
            true
        }.getOrDefault(false)
    }

    suspend fun load(context: Context): ImageBitmap? = withContext(Dispatchers.IO) {
        val f = file(context)
        if (!f.exists()) return@withContext null
        runCatching { BitmapFactory.decodeFile(f.path)?.asImageBitmap() }.getOrNull()
    }

    private fun decode(context: Context, uri: Uri): Bitmap? {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            // ImageDecoder also applies the photo's rotation (EXIF) for us.
            val source = ImageDecoder.createSource(context.contentResolver, uri)
            return ImageDecoder.decodeBitmap(source) { decoder, info, _ ->
                val longest = max(info.size.width, info.size.height)
                if (longest > MAX_SIDE) {
                    val scale = MAX_SIDE.toFloat() / longest
                    decoder.setTargetSize(
                        (info.size.width * scale).toInt(),
                        (info.size.height * scale).toInt(),
                    )
                }
                decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
            }
        }
        val resolver = context.contentResolver
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        resolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, bounds) }
        var sample = 1
        while (max(bounds.outWidth, bounds.outHeight) / sample > MAX_SIDE) sample *= 2
        val opts = BitmapFactory.Options().apply { inSampleSize = sample }
        return resolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, opts) }
    }
}
