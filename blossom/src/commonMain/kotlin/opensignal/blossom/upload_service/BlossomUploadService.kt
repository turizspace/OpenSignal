package opensignal.blossom.upload_service

import opensignal.blossom.nip96_client.Nip96Client
import opensignal.blossom.nip96_client.Nip96ServerConfig
import opensignal.blossom.nip96_client.Nip96UploadRequest
import opensignal.domain.ScreenshotUploader
import opensignal.models.ScreenshotPayload
import opensignal.models.UploadedScreenshot

class BlossomUploadService(
    private val nip96Client: Nip96Client
) : ScreenshotUploader {

    constructor(serverConfig: Nip96ServerConfig) : this(Nip96Client(serverConfig))

    override suspend fun upload(payload: ScreenshotPayload): UploadedScreenshot {
        val response = nip96Client.upload(
            Nip96UploadRequest(
                fileName = payload.name,
                mimeType = payload.mimeType,
                bytes = payload.bytes,
                alt = "OpenSignal chart screenshot",
                tags = listOf("opensignal", "chart")
            )
        )

        return UploadedScreenshot(
            url = response.url,
            sha256 = response.sha256,
            mimeType = response.mimeType,
            sizeBytes = response.sizeBytes,
            uploadedAtIso = response.createdAtIso,
            server = response.url.substringBefore("/${response.sha256}")
        )
    }
}
