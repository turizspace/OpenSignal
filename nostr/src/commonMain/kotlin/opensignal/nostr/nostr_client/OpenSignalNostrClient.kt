package opensignal.nostr.nostr_client

import kotlinx.datetime.Clock
import opensignal.domain.AuthGateway
import opensignal.domain.NostrSignalPublisher
import opensignal.models.AuthSession
import opensignal.models.LoginMethod
import opensignal.models.PublishedSignal
import opensignal.models.TradeSignal
import opensignal.nostr.event_builder.NostrEventBuilder
import opensignal.nostr.relay_manager.RelayManager
import opensignal.nostr.relay_manager.RelayConfig
import opensignal.nostr.relay_manager.RelayConstants
import opensignal.nostr.relay_manager.RelayValidator
import opensignal.nostr.signer.ExternalSignerClient
import opensignal.nostr.signer.ExternalSignerSession
import opensignal.nostr.signer.NsecSigner
import opensignal.nostr.nip65.Nip65RelayListManager

/**
 * OpenSignal Nostr Client
 * 
 * Implements:
 * - NIP-01: Core Nostr protocol
 * - NIP-46: External signer
 * - NIP-65: Relay list metadata
 */
class OpenSignalNostrClient(
    private val nsecSigner: NsecSigner = NsecSigner(),
    private val externalSignerClient: ExternalSignerClient = ExternalSignerClient(),
    private val eventBuilder: NostrEventBuilder = NostrEventBuilder(),
    private val relayManager: RelayManager = RelayManager()
) : AuthGateway, NostrSignalPublisher {

    private var currentPubkey: String? = null
    private var currentNsec: String? = null
    private var externalSession: ExternalSignerSession? = null

    override suspend fun loginWithNsec(nsec: String, relayHint: String?): AuthSession {
        require(nsecSigner.validate(nsec)) { "Invalid nsec format" }

        val pubkey = nsecSigner.derivePubkey(nsec)
        currentPubkey = pubkey
        currentNsec = nsec
        externalSession = null

        return AuthSession(
            pubkey = pubkey,
            method = LoginMethod.NSEC,
            relayHint = relayHint,
            connectedSigner = true
        )
    }

    override suspend fun loginWithExternalSigner(connectionUri: String?): AuthSession {
        val session = externalSignerClient.connect(connectionUri)
        externalSession = session
        currentNsec = null
        currentPubkey = session.pubkey

        return AuthSession(
            pubkey = session.pubkey,
            method = LoginMethod.EXTERNAL_SIGNER,
            relayHint = connectionUri,
            connectedSigner = session.connected
        )
    }

    override suspend fun loginWithExternalSignerSession(
        pubkey: String,
        packageName: String,
        relayHint: String?
    ): AuthSession {
        val session = ExternalSignerSession(
            connectionUri = packageName,
            pubkey = pubkey,
            connected = true
        )
        externalSession = session
        currentNsec = null
        currentPubkey = pubkey

        return AuthSession(
            pubkey = pubkey,
            method = LoginMethod.EXTERNAL_SIGNER,
            relayHint = relayHint,
            connectedSigner = true
        )
    }

    override suspend fun publish(signal: TradeSignal, relays: List<String>): PublishedSignal {
        val pubkey = requireNotNull(currentPubkey) {
            "Authenticate first via nsec or external signer"
        }
        relayManager.ensureConnected(relays)

        val content = "signal:${signal.id}:${signal.symbol}:${signal.generatedAtIso}"
        val signature = when {
            currentNsec != null -> nsecSigner.sign(content, currentNsec!!)
            externalSession != null -> externalSignerClient.sign(content, externalSession!!)
            else -> error("No signer configured")
        }

        val event = eventBuilder.buildTradeSignalEvent(
            signal = signal,
            pubkey = pubkey,
            signature = signature
        )

        return PublishedSignal(
            eventId = event.id,
            relays = relayManager.connectedRelays().toList(),
            publishedAtIso = Clock.System.now().toString()
        )
    }

    /**
     * Fetch user's relay preferences (NIP-65).
     * Queries relays for the user's kind 10002 event and parses relay list.
     * 
     * Implementation note: In production, this would:
     * 1. Connect to bootstrap relays
     * 2. Send a subscription with filter { kinds: [10002], authors: [pubkey], limit: 1 }
     * 3. Wait for EOSE (end-of-stored-events)
     * 4. Parse the kind 10002 event
     * 5. Extract relay list from tags
     * 
     * For now, simulates by checking relayManager state and adds bootstrap relays.
     */
    suspend fun fetchUserRelayPreferences(): List<RelayConfig> {
        val pubkey = requireNotNull(currentPubkey) { "Must be logged in to fetch relay preferences" }
        
        // Simulate fetching by combining:
        // 1. User's configured relays (from relayManager)
        // 2. Bootstrap relays (for initial discovery)
        val userConfigured = relayManager.userRelays.value
        
        val bootstrapRelays = listOf(
            RelayConfig(url = RelayConstants.PrimaryRelays.DAMUS, permissions = RelayConstants.RelayPermissions.READ_WRITE),
            RelayConfig(url = RelayConstants.PrimaryRelays.PRIMAL, permissions = RelayConstants.RelayPermissions.READ_WRITE)
        )
        
        // Merge: user configured + bootstrap (prioritize user config)
        val merged = (userConfigured + bootstrapRelays).distinctBy { it.url }
        relayManager.updateUserRelayPreferences(merged)
        
        return merged
    }

    /**
     * Update user's relay preferences (NIP-65).
     * Publishes a kind 10002 event to relays.
     */
    suspend fun updateUserRelayPreferences(relayConfigs: List<RelayConfig>): String {
        val pubkey = requireNotNull(currentPubkey) {
            "Authenticate first to update relay preferences"
        }

        val signature = when {
            currentNsec != null -> nsecSigner.sign("relays:${pubkey}", currentNsec!!)
            externalSession != null -> externalSignerClient.sign("relays:${pubkey}", externalSession!!)
            else -> error("No signer configured")
        }

        val event = eventBuilder.buildRelayListEvent(
            pubkey = pubkey,
            relays = relayConfigs,
            signature = signature
        )

        // Update local preferences
        relayManager.updateUserRelayPreferences(relayConfigs)

        // In a real implementation, publish this event to relays
        return event.id
    }

    /**
     * Share signal as a text note (kind 1).
     * Useful for manual sharing with additional context.
     */
    suspend fun shareSignalAsNote(
        signal: TradeSignal,
        relays: List<String>
    ): PublishedSignal {
        val pubkey = requireNotNull(currentPubkey) {
            "Authenticate first to share signal"
        }
        relayManager.ensureConnected(relays)

        val content = "note:${signal.id}"
        val signature = when {
            currentNsec != null -> nsecSigner.sign(content, currentNsec!!)
            externalSession != null -> externalSignerClient.sign(content, externalSession!!)
            else -> error("No signer configured")
        }

        val event = eventBuilder.buildSignalShareNote(
            signal = signal,
            pubkey = pubkey,
            signature = signature
        )

        return PublishedSignal(
            eventId = event.id,
            relays = relayManager.connectedRelays().toList(),
            publishedAtIso = Clock.System.now().toString()
        )
    }

    /**
     * Share signal to custom relay subset.
     * Allows selective relay publishing.
     */
    suspend fun shareSignalToRelays(
        signal: TradeSignal,
        selectedRelays: List<String>
    ): PublishedSignal {
        val (validRelays, invalid) = RelayValidator.validateRelayList(selectedRelays)
        require(validRelays.isNotEmpty()) { "No valid relays provided to share to. Invalid: $invalid" }

        return publish(signal, validRelays)
    }

    /**
     * Get writeable relays for current signal publishing.
     */
    fun getWriteableRelays(): List<String> {
        return relayManager.getWriteableRelays().toList()
    }

    /**
     * Get all connected relays.
     */
    fun getConnectedRelays(): List<String> {
        return relayManager.connectedRelays().toList()
    }

    /**
     * Clear authentication (logout).
     */
    fun logout() {
        currentPubkey = null
        currentNsec = null
        externalSession = null
        relayManager.clearRelays()
    }
}
