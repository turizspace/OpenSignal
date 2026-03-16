package opensignal.nostr.relay_manager

/**
 * Relay constants and presets for Nostr protocol compliance.
 * These are well-known, stable Nostr relays used for signal publishing.
 */
object RelayConstants {
    
    // Relay tier classification for fallback/redundancy logic
    enum class RelayTier {
        PRIMARY,      // Tier-1 stable relays (high uptime, performance)
        SECONDARY,    // Tier-2 reliable relays (good coverage)
        TERTIARY      // Tier-3 niche relays (specialized audience)
    }

    // Primary relays - most reliable and widely used
    object PrimaryRelays {
        const val DAMUS = "wss://relay.damus.io"
        const val PRIMAL = "wss://relay.primal.net"
        const val NOSTR_BAND = "wss://nostr.band"
        const val BLOCKCHAIN_CHAT = "wss://relay.blockchain.chat"
        
        val all = listOf(DAMUS, PRIMAL, NOSTR_BAND, BLOCKCHAIN_CHAT)
    }

    // Secondary relays - good coverage
    object SecondaryRelays {
        const val WELSHMAN = "wss://welshman.btc-library.com"
        const val NOSTRBUILD = "wss://nostr.build"
        const val NOSTR_OFFICIAL = "wss://official.nostr.com"
        
        val all = listOf(WELSHMAN, NOSTRBUILD, NOSTR_OFFICIAL)
    }

    // Tertiary/specialized relays
    object TertiaryRelays {
        const val LN_BITS = "wss://ln.bits.horse"
        const val NOSTR_SEES = "wss://nostr.sees.me"
        
        val all = listOf(LN_BITS, NOSTR_SEES)
    }

    // Default relay set for new users
    val DEFAULT_RELAYS = PrimaryRelays.all

    // All known relays (for validation and discovery)
    val ALL_KNOWN_RELAYS = PrimaryRelays.all + SecondaryRelays.all + TertiaryRelays.all

    /**
     * Get tier for a relay URL.
     */
    fun getTier(relayUrl: String): RelayTier = when (relayUrl) {
        in PrimaryRelays.all -> RelayTier.PRIMARY
        in SecondaryRelays.all -> RelayTier.SECONDARY
        else -> RelayTier.TERTIARY
    }

    /**
     * Get recommended read/write permissions for relay publishing.
     * In NIP-65, relays can have 'read', 'write', or both permissions.
     */
    object RelayPermissions {
        const val READ = "read"
        const val WRITE = "write"
        const val READ_WRITE = "read,write"
        
        // For signal publishing, we need write permission
        const val SIGNAL_PUBLISH = WRITE
    }

    // NIP-65 relay list event kind
    const val RELAY_LIST_EVENT_KIND = 10002
    
    // Standard tag name for relay in NIP-65
    const val RELAY_TAG_NAME = "r"
}

/**
 * Extended relay configuration with metadata.
 * Supports NIP-65 relay list event specification.
 */
data class RelayConfig(
    val url: String,
    val permissions: String = RelayConstants.RelayPermissions.READ_WRITE,
    val metadata: Map<String, String> = emptyMap()
) {
    fun canRead(): Boolean = permissions.contains(RelayConstants.RelayPermissions.READ)
    fun canWrite(): Boolean = permissions.contains(RelayConstants.RelayPermissions.WRITE)
}

/**
 * Relay discovery preference for network redundancy.
 */
enum class RelayDiscoveryStrategy {
    // Use only user's preferred relays
    PREFERRED_ONLY,
    
    // Use preferred + fallback to well-known primaries
    PREFERRED_WITH_FALLBACK,
    
    // Automatic discovery from NIP-65
    AUTOMATIC
}
