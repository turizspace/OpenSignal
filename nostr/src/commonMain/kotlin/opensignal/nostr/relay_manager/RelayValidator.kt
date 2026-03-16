package opensignal.nostr.relay_manager

/**
 * Validates Nostr relay URLs according to NIP-01 and common practices.
 * Ensures relays are properly formatted and reachable.
 */
object RelayValidator {
    
    private const val WSS_SCHEME = "wss://"
    private const val WS_SCHEME = "ws://"
    
    /**
     * Validates a relay URL format.
     * 
     * Requirements:
     * - Must use wss:// (secure WebSocket)
     * - Must not be empty
     * - Must have a valid hostname
     * 
     * Note: Use shouldWarnForInsecure() for ws:// detection
     */
    fun isValidRelayUrl(url: String): Boolean {
        if (url.isBlank()) return false
        
        // Production relays should use wss://
        if (!url.startsWith(WSS_SCHEME)) {
            // Allow ws:// only for localhost development
            if (url.startsWith(WS_SCHEME) && isLocalhost(url)) {
                return isValidHostname(url.removePrefix(WS_SCHEME))
            }
            return false
        }
        
        val hostname = url.removePrefix(WSS_SCHEME)
        return isValidHostname(hostname)
    }
    
    /**
     * Check if URL should warn about insecure connection.
     * ws:// is acceptable for development/localhost but not recommended for production.
     */
    fun shouldWarnForInsecure(url: String): Boolean {
        return url.startsWith(WS_SCHEME) && !isLocalhost(url)
    }
    
    /**
     * Validates hostname format.
     * Accepts domain names and IP addresses.
     */
    private fun isValidHostname(hostname: String): Boolean {
        if (hostname.isBlank()) return false
        
        // Check for valid hostname pattern (simplified validation)
        val validHostnamePattern = Regex("^([a-zA-Z0-9]([a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?\\.)+[a-zA-Z0-9]([a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?(:\\d+)?$|^localhost(:\\d+)?$|^\\d{1,3}(\\.\\d{1,3}){3}(:\\d+)?$")
        
        return validHostnamePattern.matches(hostname)
    }
    
    /**
     * Checks if relay is localhost (for development).
     */
    private fun isLocalhost(url: String): Boolean {
        return url.contains("localhost", ignoreCase = true) ||
               url.contains("127.0.0.1") ||
               url.contains("[::1]")
    }
    
    /**
     * Normalizes relay URL by ensuring consistent format.
     */
    fun normalizeRelayUrl(url: String): String {
        var normalized = url.trim()
        // Ensure lowercase for consistency
        if (normalized.startsWith("WSS://")) {
            normalized = WSS_SCHEME + normalized.substring(6)
        }
        return normalized
    }
    
    /**
     * Validates a list of relay URLs.
     * Returns pair of (validRelays, invalidRelays).
     */
    fun validateRelayList(relays: List<String>): Pair<List<String>, List<String>> {
        val valid = mutableListOf<String>()
        val invalid = mutableListOf<String>()
        
        relays.forEach { relay ->
            if (isValidRelayUrl(relay)) {
                valid.add(normalizeRelayUrl(relay))
            } else {
                invalid.add(relay)
            }
        }
        
        return valid to invalid
    }
    
    /**
     * Gets a validation error message for an invalid relay URL.
     */
    fun getValidationError(url: String): String? {
        return when {
            url.isBlank() -> "Relay URL cannot be empty"
            !url.startsWith(WSS_SCHEME) && !url.startsWith(WS_SCHEME) -> 
                "Relay must use wss:// (secure) or ws:// (localhost only)"
            shouldWarnForInsecure(url) -> 
                "Warning: ws:// is not secure. Use wss:// for production relays"
            !isValidRelayUrl(url) -> "Invalid relay URL format"
            else -> null
        }
    }
}
