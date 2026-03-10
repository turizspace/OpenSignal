package opensignal.ai.gpu_acceleration

import kotlinx.serialization.Serializable

@Serializable
data class GpuConfig(
    val enabled: Boolean = true,
    val provider: String = "auto",
    val maxBatchSize: Int = 1,
    val precision: String = "fp16"
)
