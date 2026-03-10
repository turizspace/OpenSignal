package opensignal.blossom.nip96_client

import kotlinx.datetime.Clock
import opensignal.blossom.media_hash.MediaHash

class Nip96Client(
    private val config: Nip96ServerConfig
) {

    suspend fun upload(request: Nip96UploadRequest): Nip96UploadResponse {
        val hash = MediaHash.pseudoSha256(request.bytes)
        val cleanBase = config.baseUrl.trimEnd('/')
        val url = "$cleanBase/$hash-${request.fileName}"

        return Nip96UploadResponse(
            url = url,
            sha256 = hash,
            sizeBytes = request.bytes.size.toLong(),
            mimeType = request.mimeType,
            createdAtIso = Clock.System.now().toString()
        )
    }
}
