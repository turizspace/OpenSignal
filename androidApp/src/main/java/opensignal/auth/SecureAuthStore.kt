package opensignal.auth

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import opensignal.models.AuthSession
import opensignal.models.LoginMethod

data class StoredAuthSession(
    val pubkey: String,
    val method: LoginMethod,
    val relayHint: String?,
    val nsec: String?,
    val externalPackage: String?
)

class SecureAuthStore(context: Context) {
    private val prefs = EncryptedSharedPreferences.create(
        context,
        FILE_NAME,
        MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build(),
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    fun load(): StoredAuthSession? {
        val methodRaw = prefs.getString(KEY_METHOD, null) ?: return null
        val method = runCatching { LoginMethod.valueOf(methodRaw) }.getOrNull() ?: return null
        val pubkey = prefs.getString(KEY_PUBKEY, null) ?: return null
        val relayHint = prefs.getString(KEY_RELAY_HINT, null)
        val nsec = prefs.getString(KEY_NSEC, null)
        val externalPackage = prefs.getString(KEY_EXTERNAL_PACKAGE, null)

        if (method == LoginMethod.NSEC && nsec.isNullOrBlank()) return null
        if (method == LoginMethod.EXTERNAL_SIGNER && externalPackage.isNullOrBlank()) return null

        return StoredAuthSession(
            pubkey = pubkey,
            method = method,
            relayHint = relayHint,
            nsec = nsec,
            externalPackage = externalPackage
        )
    }

    fun save(session: AuthSession, nsec: String?, externalPackage: String?) {
        prefs.edit()
            .putString(KEY_METHOD, session.method.name)
            .putString(KEY_PUBKEY, session.pubkey)
            .putString(KEY_RELAY_HINT, session.relayHint)
            .apply {
                when (session.method) {
                    LoginMethod.NSEC -> {
                        if (!nsec.isNullOrBlank()) putString(KEY_NSEC, nsec) else remove(KEY_NSEC)
                        remove(KEY_EXTERNAL_PACKAGE)
                    }
                    LoginMethod.EXTERNAL_SIGNER -> {
                        putString(KEY_EXTERNAL_PACKAGE, externalPackage)
                        remove(KEY_NSEC)
                    }
                }
            }
            .apply()
    }

    fun clear() {
        prefs.edit().clear().apply()
    }

    private companion object {
        private const val FILE_NAME = "opensignal_secure_auth"
        private const val KEY_METHOD = "method"
        private const val KEY_PUBKEY = "pubkey"
        private const val KEY_RELAY_HINT = "relay_hint"
        private const val KEY_NSEC = "nsec"
        private const val KEY_EXTERNAL_PACKAGE = "external_package"
    }
}
