package opensignal.ai.vision

import android.graphics.BitmapFactory
import android.graphics.Bitmap
import java.io.ByteArrayOutputStream

actual object ImageTools {
    actual fun size(imageBytes: ByteArray): ImageSize {
        val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size, options)
        return ImageSize(width = options.outWidth, height = options.outHeight)
    }

    actual fun crop(imageBytes: ByteArray, rect: IntRect): ByteArray {
        val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
            ?: error("Failed to decode image for crop")
        val safeRect = rect.coerce(bitmap.width, bitmap.height)
        val cropped = Bitmap.createBitmap(bitmap, safeRect.left, safeRect.top, safeRect.width, safeRect.height)
        bitmap.recycle()
        val output = ByteArrayOutputStream()
        cropped.compress(Bitmap.CompressFormat.PNG, 100, output)
        cropped.recycle()
        return output.toByteArray()
    }

    private fun IntRect.coerce(maxWidth: Int, maxHeight: Int): IntRect {
        val left = left.coerceIn(0, maxWidth - 1)
        val top = top.coerceIn(0, maxHeight - 1)
        val right = right.coerceIn(left + 1, maxWidth)
        val bottom = bottom.coerceIn(top + 1, maxHeight)
        return IntRect(left, top, right, bottom)
    }
}
