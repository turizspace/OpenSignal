package opensignal.ai.onnx_runner

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint

actual object ImageTensorPreprocessor {

    actual fun toChwFloat(
        imageBytes: ByteArray,
        targetWidth: Int,
        targetHeight: Int
    ): FloatArray {
        val decoded = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
            ?: error("Failed to decode screenshot bytes as Bitmap")

        val scaled = letterbox(decoded, targetWidth, targetHeight)
        decoded.recycle()

        val pixels = IntArray(targetWidth * targetHeight)
        scaled.getPixels(pixels, 0, targetWidth, 0, 0, targetWidth, targetHeight)

        val planeSize = targetWidth * targetHeight
        val output = FloatArray(planeSize * 3)

        var index = 0
        pixels.forEach { pixel ->
            val r = (pixel shr 16) and 0xFF
            val g = (pixel shr 8) and 0xFF
            val b = pixel and 0xFF

            output[index] = r / 255f
            output[index + planeSize] = g / 255f
            output[index + planeSize * 2] = b / 255f
            index++
        }

        scaled.recycle()
        return output
    }

    private fun letterbox(source: Bitmap, targetWidth: Int, targetHeight: Int): Bitmap {
        val scale = minOf(
            targetWidth.toFloat() / source.width.toFloat(),
            targetHeight.toFloat() / source.height.toFloat()
        )
        val scaledWidth = (source.width * scale).toInt().coerceAtLeast(1)
        val scaledHeight = (source.height * scale).toInt().coerceAtLeast(1)
        val resized = Bitmap.createScaledBitmap(source, scaledWidth, scaledHeight, true)
        val output = Bitmap.createBitmap(targetWidth, targetHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(output)
        canvas.drawColor(Color.BLACK)
        val left = (targetWidth - scaledWidth) / 2f
        val top = (targetHeight - scaledHeight) / 2f
        canvas.drawBitmap(resized, left, top, Paint(Paint.FILTER_BITMAP_FLAG))
        resized.recycle()
        return output
    }
}
