package opensignal.ai.onnx_runner

import java.awt.Color
import java.awt.RenderingHints
import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream
import javax.imageio.ImageIO

actual object ImageTensorPreprocessor {

    actual fun toChwFloat(
        imageBytes: ByteArray,
        targetWidth: Int,
        targetHeight: Int
    ): FloatArray {
        val image = ImageIO.read(ByteArrayInputStream(imageBytes))
            ?: error("Failed to decode screenshot bytes as image")

        val resized = letterbox(image, targetWidth, targetHeight)

        val planeSize = targetWidth * targetHeight
        val output = FloatArray(planeSize * 3)

        var index = 0
        for (y in 0 until targetHeight) {
            for (x in 0 until targetWidth) {
                val rgb = resized.getRGB(x, y)
                val r = (rgb shr 16) and 0xFF
                val g = (rgb shr 8) and 0xFF
                val b = rgb and 0xFF

                output[index] = r / 255f
                output[index + planeSize] = g / 255f
                output[index + planeSize * 2] = b / 255f
                index++
            }
        }

        return output
    }

    private fun letterbox(source: BufferedImage, targetWidth: Int, targetHeight: Int): BufferedImage {
        val scale = minOf(
            targetWidth.toFloat() / source.width.toFloat(),
            targetHeight.toFloat() / source.height.toFloat()
        )
        val scaledWidth = (source.width * scale).toInt().coerceAtLeast(1)
        val scaledHeight = (source.height * scale).toInt().coerceAtLeast(1)

        val output = BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_RGB)
        val graphics = output.createGraphics()
        graphics.color = Color.BLACK
        graphics.fillRect(0, 0, targetWidth, targetHeight)
        graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR)
        val x = (targetWidth - scaledWidth) / 2
        val y = (targetHeight - scaledHeight) / 2
        graphics.drawImage(source, x, y, scaledWidth, scaledHeight, null)
        graphics.dispose()
        return output
    }
}
