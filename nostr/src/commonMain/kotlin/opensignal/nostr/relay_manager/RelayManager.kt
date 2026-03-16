package opensignal.nostr.relay_manager

import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Manages Nostr relay connections and discovery.
 * 
 * Responsibilities:
 * - Connection management (establishing/tracking relays)
 * - NIP-65 relay list event discovery
 * - Relay validation and normalization
 * - Fallback relay selection
 * - Relay health/status tracking
 */
class RelayManager {

    private val _connected = MutableStateFlow<Set<String>>(emptySet())
    val connected: StateFlow<Set<String>> = _connected.asStateFlow()
    
    private val _userRelays = MutableStateFlow<List<RelayConfig>>(emptyList())
    val userRelays: StateFlow<List<RelayConfig>> = _userRelays.asStateFlow()
    
    private val _relayStatus = MutableStateFlow<Map<String, RelayStatus>>(emptyMap())
    val relayStatus: StateFlow<Map<String, RelayStatus>> = _relayStatus.asStateFlow()

    /**
     * Relay connection status.
     */
    enum class RelayStatus {
        CONNECTING,
        CONNECTED,
        DISCONNECTED,
        ERROR
    }

    /**
     * Ensure relays are connected/available with validation.
     * Duplicates are removed, invalid URLs are filtered.
     */
    fun ensureConnected(relays: List<String>) {
        val (validRelays, _) = RelayValidator.validateRelayList(relays)
        val current = _connected.value.toMutableSet()
        current.addAll(validRelays)
        _connected.value = current
    }

    /**
     * Set active relays (replaces current set).
     */
    fun setActiveRelays(relays: List<String>) {
        val (validRelays, _) = RelayValidator.validateRelayList(relays)
        _connected.value = validRelays.toSet()
    }

    /**
     * Get all currently connected relays.
     */
    fun connectedRelays(): Set<String> = _connected.value

    /**
     * Get connected relays with write permission for publishing.
     */
    fun getWriteableRelays(): Set<String> {
        val writeableConfigs = _userRelays.value.filter { it.canWrite() }
        return if (writeableConfigs.isNotEmpty()) {
            writeableConfigs.map { it.url }.toSet()
        } else {
            // Fallback to all connected relays if no user relay preferences
            connectedRelays()
        }
    }

    /**
     * Update user's relay preferences (from NIP-65 event or manual config).
     * This represents the user's preferred relay configuration.
     */
    fun updateUserRelayPreferences(relayConfigs: List<RelayConfig>) {
        _userRelays.value = relayConfigs
        // Also update connected relays to include user preferences
        ensureConnected(relayConfigs.map { it.url })
    }

    /**
     * Add a relay to connection list.
     */
    fun addRelay(relayUrl: String): Boolean {
        if (!RelayValidator.isValidRelayUrl(relayUrl)) return false
        val normalized = RelayValidator.normalizeRelayUrl(relayUrl)
        val current = _connected.value.toMutableSet()
        current.add(normalized)
        _connected.value = current
        return true
    }

    /**
     * Remove a relay from connection list.
     */
    fun removeRelay(relayUrl: String): Boolean {
        val current = _connected.value.toMutableSet()
        return if (current.remove(relayUrl)) {
            _connected.value = current
            true
        } else {
            false
        }
    }

    /**
     * Update relay status (for UI feedback).
     */
    fun updateRelayStatus(relayUrl: String, status: RelayStatus) {
        val current = _relayStatus.value.toMutableMap()
        current[relayUrl] = status
        _relayStatus.value = current
    }

    /**
     * Get relay tier for prioritization.
     */
    fun getRelayTier(relayUrl: String): RelayConstants.RelayTier {
        return RelayConstants.getTier(relayUrl)
    }

    /**
     * Get relays sorted by tier (primary first, for fallback logic).
     */
    fun getRelaysByTier(): List<String> {
        return connectedRelays()
            .sortedWith(compareBy { RelayConstants.getTier(it).ordinal })
    }

    /**
     * Clear all relay connections.
     */
    fun clearRelays() {
        _connected.value = emptySet()
        _userRelays.value = emptyList()
        _relayStatus.value = emptyMap()
    }
}
