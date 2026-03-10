package opensignal.nostr.event_builder

import kotlinx.datetime.Clock
import opensignal.models.TradeSignal
import opensignal.signals.SignalJsonSerializer

data class NostrEvent(
    val id: String,
    val pubkey: String,
    val kind: Int,
    val createdAt: Long,
    val tags: List<List<String>>,
    val content: String,
    val sig: String
)

class NostrEventBuilder {

    fun buildTradeSignalEvent(
        signal: TradeSignal,
        pubkey: String,
        signature: String
    ): NostrEvent {
        val content = SignalJsonSerializer.toJson(signal)
        val createdAt = Clock.System.now().epochSeconds
        val id = buildEventId(pubkey, content, createdAt)

        return NostrEvent(
            id = id,
            pubkey = pubkey,
            kind = 30315,
            createdAt = createdAt,
            tags = listOf(
                listOf("t", "opensignal"),
                listOf("symbol", signal.symbol),
                listOf("timeframe", signal.timeframe.name),
                listOf("screenshot", signal.screenshot.sha256)
            ),
            content = content,
            sig = signature
        )
    }

    private fun buildEventId(pubkey: String, content: String, createdAt: Long): String {
        return "${pubkey}_${createdAt}_${content.hashCode().toUInt().toString(16)}"
    }
}
