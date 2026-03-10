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
import opensignal.nostr.signer.ExternalSignerClient
import opensignal.nostr.signer.ExternalSignerSession
import opensignal.nostr.signer.NsecSigner

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
}
