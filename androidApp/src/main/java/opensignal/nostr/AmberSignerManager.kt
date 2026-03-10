package opensignal.nostr

import android.content.Intent
import android.net.Uri
import opensignal.util.SecureLog
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.withTimeout
import java.util.concurrent.ConcurrentHashMap

/**
 * AmberSignerManager — handles communication with external Nostr signer apps (Amber/Quartz).
 * Supports getting pubkey and signing events (NIP-46 compatible).
 */
object AmberSignerManager {
    private var externalPubkey: String? = null
    private var externalPackage: String? = null
    private var launcher: ((Intent) -> Unit)? = null

    // Keep public for coroutine waiting
    val pending = ConcurrentHashMap<String, CompletableDeferred<IntentResult>>()

    // Track request types to distinguish login from signing
    private val requestTypes = ConcurrentHashMap<String, String>()

    data class IntentResult(
        val id: String? = null,
        val result: String? = null,
        val event: String? = null,
        val pubkey: String? = null,
        val packageName: String? = null,
    )

    fun registerActivityLauncher(l: (Intent) -> Unit) {
        launcher = l
    }

    fun unregisterActivityLauncher(l: (Intent) -> Unit) {
        if (launcher == l) launcher = null
    }

    fun handleIntentResponse(intent: Intent) {
        try {
            val uri = intent.data
            var id = intent.getStringExtra("id") ?: uri?.getQueryParameter("id")
            var result = intent.getStringExtra("result") ?: uri?.getQueryParameter("result")
            var event = intent.getStringExtra("event") ?: uri?.getQueryParameter("event")
            var pubkey = intent.getStringExtra("pubkey")
                ?: intent.getStringExtra("npub")
                ?: uri?.getQueryParameter("pubkey")
                ?: uri?.getQueryParameter("npub")
            val pkg = intent.getStringExtra("package") ?: uri?.getQueryParameter("package")

            val schemePayload = uri?.schemeSpecificPart?.trim()
            if ((id.isNullOrBlank() || result == null || event == null || pubkey == null) &&
                schemePayload != null &&
                schemePayload.startsWith("{")
            ) {
                try {
                    val json = org.json.JSONObject(schemePayload)
                    if (id.isNullOrBlank()) id = json.optString("id").takeIf { it.isNotBlank() }
                    if (result == null) result = json.optString("result").takeIf { it.isNotBlank() }
                    if (event == null) event = json.optString("event").takeIf { it.isNotBlank() }
                    if (pubkey == null) {
                        pubkey = json.optString("pubkey").takeIf { it.isNotBlank() }
                            ?: json.optString("npub").takeIf { it.isNotBlank() }
                    }
                } catch (_: Exception) {
                    // ignore invalid payloads
                }
            }

            if (id.isNullOrBlank()) return

            if (result != null && result.length > 10000) {
                SecureLog.w("Received intent with suspiciously large result - ignoring")
                return
            }

            if (event != null) {
                try {
                    org.json.JSONObject(event)
                    if (event.length > 100000) {
                        SecureLog.w("Received intent with suspiciously large event - ignoring")
                        return
                    }
                } catch (e: Exception) {
                    val trimmed = event.trim()
                    val looksLikeNpub = trimmed.startsWith("npub1")
                    val looksLikeHexPubkey = (
                        trimmed.length == 64 && trimmed.all { it.isDigit() || it.lowercaseChar() in 'a'..'f' }
                    )
                    val isPubkeyPayload = looksLikeNpub || looksLikeHexPubkey
                    if (isPubkeyPayload) {
                        if (pubkey == null) pubkey = trimmed
                    } else {
                        SecureLog.w("Received intent with invalid event JSON - ignoring event field")
                        event = null
                    }
                }
            }

            if (pubkey == null && result != null) {
                val trimmed = result.trim()
                if (trimmed.startsWith("{")) {
                    try {
                        val json = org.json.JSONObject(trimmed)
                        pubkey = json.optString("pubkey").takeIf { it.isNotBlank() }
                            ?: json.optString("npub").takeIf { it.isNotBlank() }
                    } catch (_: Exception) {
                        // ignore invalid json
                    }
                }
            }

            val res = IntentResult(id = id, result = result, event = event, pubkey = pubkey, packageName = pkg)

            val deferred = pending.remove(id)
            if (deferred != null) {
                deferred.complete(res)
            } else {
                SecureLog.w("Received response for unknown request ID: ${id.take(8)}")
            }
        } catch (e: Exception) {
            SecureLog.w("Error handling intent response: ${e.message}")
        }
    }

    fun configure(pubkeyHex: String, packageName: String) {
        externalPubkey = pubkeyHex
        externalPackage = packageName
    }

    fun getConfiguredPubkey(): String? = externalPubkey

    fun getConfiguredPackage(): String? = externalPackage

    fun isLoginRequest(requestId: String): Boolean {
        return requestTypes[requestId] == "get_public_key"
    }

    suspend fun requestPublicKey(
        timeoutMs: Long = 60_000,
        packageName: String? = null
    ): IntentResult {
        val pkg = packageName?.takeIf { it.isNotBlank() } ?: "com.greenart7c3.nostrsigner"
        val l = launcher ?: throw IllegalStateException("No launcher registered")

        val callId = java.util.UUID.randomUUID().toString().replace("-", "").take(32)
        val deferred = CompletableDeferred<IntentResult>()
        pending[callId] = deferred
        requestTypes[callId] = "get_public_key"

        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("nostrsigner:")).apply {
            putExtra("type", "get_public_key")
            putExtra("permissions", "[]")
            `package` = pkg
            putExtra("id", callId)
            addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }

        l(intent)

        return try {
            withTimeout(timeoutMs) { deferred.await() }
        } finally {
            pending.remove(callId)
            requestTypes.remove(callId)
        }
    }

    suspend fun signEvent(eventJson: String, eventId: String, timeoutMs: Long = 60_000): IntentResult {
        val pkg = externalPackage ?: throw IllegalStateException("Amber package not set")
        val pub = externalPubkey ?: throw IllegalStateException("Amber pubkey not set")
        val l = launcher ?: throw IllegalStateException("No launcher registered")

        val callId = java.util.UUID.randomUUID().toString().replace("-", "").take(32)
        val deferred = CompletableDeferred<IntentResult>()
        pending[callId] = deferred
        requestTypes[callId] = "sign_event"

        val uri = Uri.parse("nostrsigner:$eventJson")
        val intent = Intent(Intent.ACTION_VIEW, uri).apply {
            `package` = pkg
            putExtra("type", "sign_event")
            putExtra("current_user", pub)
            putExtra("id", callId)
            putExtra("event", eventJson)
            addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }

        l(intent)

        return try {
            withTimeout(timeoutMs) { deferred.await() }
        } finally {
            pending.remove(callId)
            requestTypes.remove(callId)
        }
    }
}
