package opensignal.ai.vision

import android.graphics.BitmapFactory
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

actual object OcrEngine {
    actual suspend fun recognize(imageBytes: ByteArray): OcrResult {
        val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
            ?: run {
                val size = ImageTools.size(imageBytes)
                return OcrResult(words = emptyList(), imageWidth = size.width, imageHeight = size.height)
            }

        val image = InputImage.fromBitmap(bitmap, 0)
        val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

        return suspendCancellableCoroutine { continuation ->
            recognizer.process(image)
                .addOnSuccessListener { result ->
                    val words = result.textBlocks.flatMap { block ->
                        block.lines.flatMap { line ->
                            line.elements.mapNotNull { element ->
                                val box = element.boundingBox ?: return@mapNotNull null
                                OcrWord(
                                    text = element.text,
                                    box = IntRect(box.left, box.top, box.right, box.bottom),
                                    confidence = 1.0f
                                )
                            }
                        }
                    }
                    continuation.resume(
                        OcrResult(
                            words = words,
                            imageWidth = bitmap.width,
                            imageHeight = bitmap.height
                        )
                    )
                }
                .addOnFailureListener {
                    continuation.resume(
                        OcrResult(
                            words = emptyList(),
                            imageWidth = bitmap.width,
                            imageHeight = bitmap.height
                        )
                    )
                }
        }
    }
}
