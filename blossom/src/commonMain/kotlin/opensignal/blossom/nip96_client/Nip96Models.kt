package opensignal.blossom.nip96_client

import kotlinx.serialization.Serializable

@Serializable
data class Nip96ServerConfig(
    val baseUrl: String,
    val uploadPath: String = "/api/v1/media",
    val authToken: String? = null
)

@Serializable
data class Nip96UploadRequest(
    val fileName: String,
    val mimeType: String,
    val bytes: ByteArray,
    val alt: String? = null,
    val tags: List<String> = emptyList()
)

@Serializable
data class Nip96UploadResponse(
    val url: String,
    val sha256: String,
    val sizeBytes: Long,
    val mimeType: String,
    val createdAtIso: String
)
