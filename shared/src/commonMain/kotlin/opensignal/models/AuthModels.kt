package opensignal.models

import kotlinx.serialization.Serializable

@Serializable
enum class LoginMethod {
    NSEC,
    EXTERNAL_SIGNER
}

@Serializable
data class AuthSession(
    val pubkey: String,
    val method: LoginMethod,
    val relayHint: String?,
    val connectedSigner: Boolean
)
