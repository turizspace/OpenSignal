package opensignal.ai.vision

import java.io.ByteArrayInputStream
import javax.imageio.ImageIO
import net.sourceforge.tess4j.ITessAPI
import net.sourceforge.tess4j.Tesseract

actual object OcrEngine {
    actual suspend fun recognize(imageBytes: ByteArray): OcrResult {
        val image = ImageIO.read(ByteArrayInputStream(imageBytes))
            ?: run {
                val size = ImageTools.size(imageBytes)
                return OcrResult(words = emptyList(), imageWidth = size.width, imageHeight = size.height)
            }

        val dataPath = resolveTessdataPath() ?: return OcrResult(
            words = emptyList(),
            imageWidth = image.width,
            imageHeight = image.height
        )

        val tesseract = Tesseract().apply {
            setDatapath(dataPath)
            setLanguage("eng")
        }

        val words = tesseract.getWords(image, ITessAPI.TessPageIteratorLevel.RIL_WORD).mapNotNull { word ->
            val box = word.boundingBox ?: return@mapNotNull null
            OcrWord(
                text = word.text,
                box = IntRect(box.x, box.y, box.x + box.width, box.y + box.height),
                confidence = (word.confidence / 100f).coerceIn(0f, 1f)
            )
        }

        return OcrResult(words = words, imageWidth = image.width, imageHeight = image.height)
    }

    private fun resolveTessdataPath(): String? {
        val env = System.getenv("TESSDATA_PREFIX")
        if (!env.isNullOrBlank()) return env

        val userDir = System.getProperty("user.dir") ?: return null
        val local = java.io.File(userDir, "tessdata")
        if (local.exists()) return local.absolutePath

        val project = java.io.File(userDir, "ai-inference/tessdata")
        if (project.exists()) return project.absolutePath

        return null
    }
}
