package opensignal.ai.vision

data class ImageSize(val width: Int, val height: Int)

expect object ImageTools {
    fun size(imageBytes: ByteArray): ImageSize
    fun crop(imageBytes: ByteArray, rect: IntRect): ByteArray
}
