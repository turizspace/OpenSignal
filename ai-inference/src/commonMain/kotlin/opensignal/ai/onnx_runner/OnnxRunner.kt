package opensignal.ai.onnx_runner

import opensignal.ai.gpu_acceleration.GpuConfig
import opensignal.ai.model_loader.ModelSpec

data class ModelHandle(
    val spec: ModelSpec,
    internal val session: OnnxSession
)

class OnnxRunner(
    private val gpuConfig: GpuConfig = GpuConfig()
) {

    fun load(spec: ModelSpec): ModelHandle {
        val session = OnnxSession(
            modelPath = spec.path,
            gpuConfig = gpuConfig
        )
        return ModelHandle(spec = spec, session = session)
    }

    fun run(
        model: ModelHandle,
        input: FloatArray,
        inputShape: LongArray
    ): FloatArray {
        return model.session.run(input = input, inputShape = inputShape)
    }

    fun close(model: ModelHandle) {
        model.session.close()
    }
}

expect class OnnxSession(modelPath: String, gpuConfig: GpuConfig) {
    fun run(input: FloatArray, inputShape: LongArray): FloatArray
    fun close()
}
