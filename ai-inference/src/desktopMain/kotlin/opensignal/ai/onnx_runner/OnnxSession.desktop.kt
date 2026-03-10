package opensignal.ai.onnx_runner

import ai.onnxruntime.OnnxTensor
import ai.onnxruntime.OrtEnvironment
import ai.onnxruntime.OrtException
import ai.onnxruntime.OrtSession
import java.io.File
import java.nio.FloatBuffer
import opensignal.ai.gpu_acceleration.GpuConfig

actual class OnnxSession actual constructor(modelPath: String, gpuConfig: GpuConfig) {

    private val environment: OrtEnvironment = OrtEnvironment.getEnvironment()
    private val options: OrtSession.SessionOptions = OrtSession.SessionOptions().apply {
        setOptimizationLevel(OrtSession.SessionOptions.OptLevel.ALL_OPT)
        if (gpuConfig.enabled && gpuConfig.provider.equals("cuda", ignoreCase = true)) {
            tryEnableCuda(this)
        }
    }
    private val resolvedModelPath = resolveModelPath(modelPath)
    private val session: OrtSession = environment.createSession(resolvedModelPath, options)
    private val inputName: String = session.inputNames.first()

    actual fun run(input: FloatArray, inputShape: LongArray): FloatArray {
        val expectedSize = inputShape.fold(1L) { acc, value -> acc * value }.toInt()
        require(expectedSize == input.size) {
            "Input size ${input.size} does not match shape product $expectedSize for model $resolvedModelPath"
        }

        val tensor = OnnxTensor.createTensor(environment, FloatBuffer.wrap(input), inputShape)
        val outputs = session.run(mapOf(inputName to tensor))

        return try {
            val firstOutput = outputs.iterator().next().value
            val outputValue = firstOutput.value
            flatten(outputValue)
        } finally {
            outputs.close()
            tensor.close()
        }
    }

    actual fun close() {
        session.close()
        options.close()
    }

    private fun resolveModelPath(modelPath: String): String {
        val direct = File(modelPath)
        if (direct.exists()) {
            return direct.absolutePath
        }

        val userDir = File(System.getProperty("user.dir") ?: ".")
        val rooted = File(userDir, modelPath)
        if (rooted.exists()) {
            return rooted.absolutePath
        }

        val parentMatch = findInParents(userDir, modelPath)
        if (parentMatch != null) {
            return parentMatch.absolutePath
        }

        throw IllegalStateException(
            "ONNX model not found at '$modelPath'. Configure Model Files in Settings or place models at models/*.onnx"
        )
    }

    private fun findInParents(start: File, relativePath: String): File? {
        var current: File? = start
        repeat(6) {
            val candidate = current?.let { File(it, relativePath) }
            if (candidate != null && candidate.exists()) {
                return candidate
            }
            current = current?.parentFile
        }
        return null
    }

    private fun flatten(value: Any?): FloatArray {
        val flattened = mutableListOf<Float>()
        collect(value, flattened)
        return flattened.toFloatArray()
    }

    private fun collect(value: Any?, output: MutableList<Float>) {
        when (value) {
            null -> Unit
            is FloatArray -> value.forEach(output::add)
            is DoubleArray -> value.forEach { output += it.toFloat() }
            is LongArray -> value.forEach { output += it.toFloat() }
            is IntArray -> value.forEach { output += it.toFloat() }
            is Array<*> -> value.forEach { collect(it, output) }
            is Number -> output += value.toFloat()
            else -> error("Unsupported ONNX output type: ${value::class.qualifiedName}")
        }
    }

    private fun tryEnableCuda(options: OrtSession.SessionOptions) {
        try {
            val method = options.javaClass.methods.firstOrNull { it.name == "addCUDA" }
            if (method != null) {
                if (method.parameterCount == 1) {
                    method.invoke(options, 0)
                } else {
                    method.invoke(options)
                }
            }
        } catch (_: OrtException) {
            // CPU execution remains active.
        } catch (_: Throwable) {
            // CPU execution remains active.
        }
    }
}
