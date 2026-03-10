package opensignal.ai.onnx_runner

import ai.onnxruntime.OnnxTensor
import ai.onnxruntime.OrtEnvironment
import ai.onnxruntime.OrtException
import ai.onnxruntime.OrtSession
import android.content.Context
import java.io.File
import java.nio.FloatBuffer
import opensignal.ai.gpu_acceleration.GpuConfig

actual class OnnxSession actual constructor(modelPath: String, gpuConfig: GpuConfig) {

    private val environment: OrtEnvironment = OrtEnvironment.getEnvironment()
    private val options: OrtSession.SessionOptions = OrtSession.SessionOptions().apply {
        setOptimizationLevel(OrtSession.SessionOptions.OptLevel.ALL_OPT)
        if (gpuConfig.enabled && gpuConfig.provider.equals("nnapi", ignoreCase = true)) {
            tryEnableNnapi(this)
        }
    }
    private val session: OrtSession = createSession(modelPath, options)
    private val inputName: String = session.inputNames.first()

    actual fun run(input: FloatArray, inputShape: LongArray): FloatArray {
        val expectedSize = inputShape.fold(1L) { acc, value -> acc * value }.toInt()
        require(expectedSize == input.size) {
            "Input size ${input.size} does not match shape product $expectedSize"
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

    private fun createSession(modelPath: String, options: OrtSession.SessionOptions): OrtSession {
        val modelFile = resolveModelFile(modelPath)
        return environment.createSession(modelFile.readBytes(), options)
    }

    private fun resolveModelFile(modelPath: String): File {
        val direct = File(modelPath)
        if (direct.exists()) {
            return direct
        }

        val appStorage = loadFromAppStorage(modelPath)
        if (appStorage != null) {
            return appStorage
        }

        val assetCopy = copyFromAssets(modelPath)
        if (assetCopy != null) {
            return assetCopy
        }

        val rooted = File(System.getProperty("user.dir") ?: ".", modelPath)
        if (rooted.exists()) {
            return rooted
        }

        throw IllegalStateException(
            "ONNX model not found at '$modelPath'. Configure Model Files in Settings or place models in app storage."
        )
    }

    private fun loadFromAppStorage(modelPath: String): File? {
        return try {
            val contextClass = Class.forName("android.app.ActivityThread")
            val method = contextClass.getMethod("currentApplication")
            val context = method.invoke(null) as? Context ?: return null
            val fileName = File(modelPath).name
            val modelsDir = File(context.filesDir, "models")
            val candidate = File(modelsDir, fileName)
            if (candidate.exists()) candidate else null
        } catch (_: Throwable) {
            null
        }
    }

    private fun copyFromAssets(modelPath: String): File? {
        return try {
            val contextClass = Class.forName("android.app.ActivityThread")
            val method = contextClass.getMethod("currentApplication")
            val context = method.invoke(null) as? Context ?: return null
            val fileName = File(modelPath).name
            val assetPath = "models/$fileName"
            val modelsDir = File(context.filesDir, "models").apply { mkdirs() }
            val target = File(modelsDir, fileName)
            if (target.exists()) return target
            context.assets.open(assetPath).use { input ->
                target.outputStream().use { output -> input.copyTo(output) }
            }
            if (target.exists()) target else null
        } catch (_: Throwable) {
            null
        }
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

    private fun tryEnableNnapi(options: OrtSession.SessionOptions) {
        try {
            val method = options.javaClass.methods.firstOrNull { it.name == "addNnapi" }
            method?.invoke(options)
        } catch (_: OrtException) {
            // CPU execution remains active.
        } catch (_: Throwable) {
            // CPU execution remains active.
        }
    }
}
