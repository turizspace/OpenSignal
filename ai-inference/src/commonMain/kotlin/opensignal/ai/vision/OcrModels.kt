package opensignal.ai.vision

data class OcrWord(
    val text: String,
    val box: IntRect,
    val confidence: Float
)

data class OcrResult(
    val words: List<OcrWord>,
    val imageWidth: Int,
    val imageHeight: Int
)

expect object OcrEngine {
    suspend fun recognize(imageBytes: ByteArray): OcrResult
}
