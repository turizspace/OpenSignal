package opensignal.blossom

import opensignal.blossom.nip96_client.Nip96ServerConfig
import opensignal.blossom.upload_service.BlossomUploadService

typealias BlossomClient = BlossomUploadService

fun defaultBlossomClient(serverUrl: String): BlossomUploadService {
    return BlossomUploadService(
        Nip96ServerConfig(baseUrl = serverUrl)
    )
}
