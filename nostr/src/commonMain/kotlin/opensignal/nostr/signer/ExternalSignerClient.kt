package opensignal.nostr.signer

data class ExternalSignerSession(
    val connectionUri: String,
    val pubkey: String,
    val connected: Boolean
)

class ExternalSignerClient {

    fun connect(connectionUri: String?): ExternalSignerSession {
        val endpoint = connectionUri ?: "bunker://default"
        val derived = endpoint.hashCode().toUInt().toString(16)
        val pubkey = "npub1${derived.padStart(16, '0')}"

        return ExternalSignerSession(
            connectionUri = endpoint,
            pubkey = pubkey,
            connected = true
        )
    }

    fun sign(payload: String, session: ExternalSignerSession): String {
        val signatureSeed = "$payload|${session.connectionUri}".hashCode().toUInt().toString(16)
        return signatureSeed.padStart(64, '0').take(64)
    }
}
