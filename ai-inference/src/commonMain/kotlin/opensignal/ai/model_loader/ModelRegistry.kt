package opensignal.ai.model_loader

import kotlinx.serialization.Serializable

@Serializable
data class ModelSpec(
    val name: String,
    val path: String,
    val version: String,
    val inputWidth: Int,
    val inputHeight: Int,
    val inputChannels: Int,
    val outputSize: Int
)

class ModelRegistry(
    private val overrides: Map<String, String> = emptyMap()
) {

    private val models = mapOf(
        "candle_detector" to ModelSpec(
            name = "candle_detector",
            path = "models/candle_detector.onnx",
            version = "v1.0.0",
            inputWidth = 640,
            inputHeight = 640,
            inputChannels = 3,
            outputSize = 256
        ),
        "liquidity_sweep" to ModelSpec(
            name = "liquidity_sweep",
            path = "models/liquidity_sweep.onnx",
            version = "v1.0.0",
            inputWidth = 640,
            inputHeight = 640,
            inputChannels = 3,
            outputSize = 64
        ),
        "structure_detector" to ModelSpec(
            name = "structure_detector",
            path = "models/structure_detector.onnx",
            version = "v1.0.0",
            inputWidth = 640,
            inputHeight = 640,
            inputChannels = 3,
            outputSize = 64
        ),
        "trend_classifier" to ModelSpec(
            name = "trend_classifier",
            path = "models/trend_classifier.onnx",
            version = "v1.0.0",
            inputWidth = 224,
            inputHeight = 224,
            inputChannels = 3,
            outputSize = 3
        )
    )

    fun get(name: String): ModelSpec {
        val base = requireNotNull(models[name]) { "Model not found: $name" }
        val overridePath = overrides[name].orEmpty()
        return if (overridePath.isBlank()) base else base.copy(path = overridePath)
    }

    fun all(): List<ModelSpec> = models.values.toList()
}
