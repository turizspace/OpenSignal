package opensignal.nostr.event_builder

import kotlinx.datetime.Clock
import opensignal.models.TradeSignal
import opensignal.signals.SignalJsonSerializer
import opensignal.nostr.relay_manager.RelayConfig

data class NostrEvent(
    val id: String,
    val pubkey: String,
    val kind: Int,
    val createdAt: Long,
    val tags: List<List<String>>,
    val content: String,
    val sig: String
)

/**
 * Nostr Event Builder
 * 
 * Supports building multiple event kinds:
 * - Kind 30315: Parameterized replaceable events for OpenSignal trade signals
 * - Kind 10002: NIP-65 relay list metadata events
 * - Kind 1: Text notes for signal sharing
 */
class NostrEventBuilder {

    /**
     * Build a trade signal event (kind 30315).
     * Parameterized replaceable event scoped by (pubkey, kind, d-tag_value).
     */
    fun buildTradeSignalEvent(
        signal: TradeSignal,
        pubkey: String,
        signature: String,
        createdAt: Long = Clock.System.now().epochSeconds
    ): NostrEvent {
        val content = SignalJsonSerializer.toJson(signal)
        val id = buildEventId(pubkey, content, createdAt)

        return NostrEvent(
            id = id,
            pubkey = pubkey,
            kind = 30315,
            createdAt = createdAt,
            tags = listOf(
                listOf("d", "${signal.symbol}_${signal.timeframe.name}"), // NIP-33: d-tag for replaceable events
                listOf("t", "opensignal"),
                listOf("symbol", signal.symbol),
                listOf("timeframe", signal.timeframe.name),
                listOf("screenshot", signal.screenshot.sha256)
            ),
            content = content,
            sig = signature
        )
    }
    
    /**
     * Build a NIP-65 relay list event (kind 10002).
     * Replaceable event for user's preferred relays.
     */
    fun buildRelayListEvent(
        pubkey: String,
        relays: List<RelayConfig>,
        signature: String,
        createdAt: Long = Clock.System.now().epochSeconds
    ): NostrEvent {
        val tags = relays.map { relay ->
            listOf("r", relay.url, relay.permissions)
        }
        
        val id = buildEventId(pubkey, "", createdAt) // NIP-65 events don't include tags in ID
        
        return NostrEvent(
            id = id,
            pubkey = pubkey,
            kind = 10002,
            createdAt = createdAt,
            tags = tags,
            content = "", // NIP-65: content is always empty
            sig = signature
        )
    }
    
    /**
     * Build a text note event (kind 1) for manual signal sharing.
     * Allows sharing signal metadata as a human-readable note.
     */
    fun buildSignalShareNote(
        signal: TradeSignal,
        pubkey: String,
        signature: String,
        createdAt: Long = Clock.System.now().epochSeconds
    ): NostrEvent {
        val content = buildSignalShareContent(signal)
        val id = buildEventId(pubkey, content, createdAt)
        
        return NostrEvent(
            id = id,
            pubkey = pubkey,
            kind = 1, // Text note
            createdAt = createdAt,
            tags = listOf(
                listOf("t", "opensignal"),
                listOf("symbol", signal.symbol),
                listOf("timeframe", signal.timeframe.name)
            ),
            content = content,
            sig = signature
        )
    }

    private fun buildSignalShareContent(signal: TradeSignal): String {
        val technical = signal.technical
        val trend = technical.trend.name
        val symbol = signal.symbol
        val timeframe = signal.timeframe.name
        val confidence = "${(signal.confidence * 100).toInt()}%"
        
        return """
            📊 OpenSignal Trade Analysis
            
            $symbol • $timeframe
            Trend: $trend
            Confidence: $confidence
            
            ${technical.summary}
        """.trimIndent()
    }

    private fun buildEventId(pubkey: String, content: String, createdAt: Long): String {
        return "${pubkey}_${createdAt}_${content.hashCode().toUInt().toString(16)}"
    }
}
