package opensignal.ai.vision

import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import javax.imageio.ImageIO

actual object ImageTools {
    actual fun size(imageBytes: ByteArray): ImageSize {
        val image = ImageIO.read(ByteArrayInputStream(imageBytes))
            ?: error("Failed to decode image for size")
        return ImageSize(width = image.width, height = image.height)
    }

    actual fun crop(imageBytes: ByteArray, rect: IntRect): ByteArray {
        val image = ImageIO.read(ByteArrayInputStream(imageBytes))
            ?: error("Failed to decode image for crop")
        val safe = rect.coerce(image.width, image.height)
        val sub = image.getSubimage(safe.left, safe.top, safe.width, safe.height)
        val output = ByteArrayOutputStream()
        ImageIO.write(sub, "png", output)
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
