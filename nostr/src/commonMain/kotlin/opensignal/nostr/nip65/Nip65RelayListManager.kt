package opensignal.nostr.nip65

import opensignal.nostr.event_builder.NostrEvent
import opensignal.nostr.relay_manager.RelayConfig
import opensignal.nostr.relay_manager.RelayConstants
import kotlinx.datetime.Clock

/**
 * NIP-65 - Relay List Metadata
 * https://github.com/nostr-protocol/nips/blob/master/65.md
 * 
 * Kind 10002 events are used to store user's publicly recommended relays.
 * 
 * Event structure:
 * - kind: 10002 (parameterized replaceable event, per-user)
 * - tags: ["r", relay_url, (marker: "read" | "write" | optional)]
 * - content: "" (empty string)
 */
object Nip65RelayListManager {
    
    /**
     * Builds a NIP-65 relay list event for publishing user's preferred relays.
     * This event is replaceable (kind 10002) and scoped to the user.
     */
    fun buildRelayListEvent(
        pubkey: String,
        relays: List<RelayConfig>,
        signature: String,
        createdAt: Long = Clock.System.now().epochSeconds
    ): NostrEvent {
        val tags = relays.map { relay ->
            // Tag format: ["r", relay_url, permission]
            listOf(
                RelayConstants.RELAY_TAG_NAME,
                relay.url,
                relay.permissions
            )
        }
        
        val id = buildEventId(pubkey, tags, createdAt)
        
        return NostrEvent(
            id = id,
            pubkey = pubkey,
            kind = RelayConstants.RELAY_LIST_EVENT_KIND,
            createdAt = createdAt,
            tags = tags,
            content = "", // NIP-65: content is empty for relay list
            sig = signature
        )
    }
    
    /**
     * Parse a NIP-65 relay list event to extract relay configurations.
     * 
     * Returns list of RelayConfig extracted from "r" tags.
     */
    fun parseRelayListEvent(event: NostrEvent): List<RelayConfig> {
        if (event.kind != RelayConstants.RELAY_LIST_EVENT_KIND) {
            return emptyList()
        }
        
        return event.tags
            .filter { it.isNotEmpty() && it[0] == RelayConstants.RELAY_TAG_NAME }
            .mapNotNull { tag ->
                if (tag.size >= 2) {
                    val url = tag[1]
                    val permissions = if (tag.size >= 3) tag[2] else RelayConstants.RelayPermissions.READ_WRITE
                    
                    // Validate relay URL
                    if (isValidRelayUrl(url)) {
                        RelayConfig(
                            url = url,
                            permissions = permissions,
                            metadata = emptyMap() // Could be extended with additional metadata
                        )
                    } else {
                        null
                    }
                } else {
                    null
                }
            }
    }
    
    /**
     * Create a displayable relay list summary from parsed relays.
     */
    fun getRelayListSummary(relays: List<RelayConfig>): String {
        if (relays.isEmpty()) return "No relays configured"
        
        val readCount = relays.count { it.canRead() }
        val writeCount = relays.count { it.canWrite() }
        
        return "$readCount read, $writeCount write • ${relays.size} total"
    }
    
    /**
     * Filter relays by permission.
     */
    fun getWritableRelays(relays: List<RelayConfig>): List<RelayConfig> {
        return relays.filter { it.canWrite() }
    }
    
    fun getReadableRelays(relays: List<RelayConfig>): List<RelayConfig> {
        return relays.filter { it.canRead() }
    }
    
    /**
     * Check if relay list is valid for publishing signals.
     * Should have at least one writeable relay.
     */
    fun isValidForPublishing(relays: List<RelayConfig>): Boolean {
        return relays.any { it.canWrite() }
    }
    
    private fun buildEventId(pubkey: String, tags: List<List<String>>, createdAt: Long): String {
        val tagsStr = tags.joinToString(",") { it.joinToString(":") }
        return "${pubkey}_${createdAt}_${tagsStr.hashCode().toUInt().toString(16)}"
    }
    
    private fun isValidRelayUrl(url: String): Boolean {
        return url.isNotBlank() && (url.startsWith("wss://") || url.startsWith("ws://"))
    }
}

/**
 * NIP-65 event query result model.
 * Represents a user's fetched relay list from a relay.
 */
data class UserRelayListEvent(
    val pubkey: String,
    val relayConfigs: List<RelayConfig>,
    val createdAt: Long,
    val eventId: String
) {
    fun getSummary(): String = Nip65RelayListManager.getRelayListSummary(relayConfigs)
}
