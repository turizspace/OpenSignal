package opensignal.models

import kotlinx.serialization.Serializable

@Serializable
data class UploadedScreenshot(
    val url: String,
    val sha256: String,
    val mimeType: String,
    val sizeBytes: Long,
    val uploadedAtIso: String,
    val server: String
)

@Serializable
data class ScreenshotPayload(
    val name: String,
    val mimeType: String,
    val bytes: ByteArray
)
