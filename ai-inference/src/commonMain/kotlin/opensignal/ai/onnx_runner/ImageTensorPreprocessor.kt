package opensignal.ai.onnx_runner

expect object ImageTensorPreprocessor {
    fun toChwFloat(
        imageBytes: ByteArray,
        targetWidth: Int,
        targetHeight: Int
    ): FloatArray
}
