package opensignal.nostr.relay_manager

class RelayManager {

    private val connected = linkedSetOf<String>()

    fun ensureConnected(relays: List<String>) {
        relays.forEach { relay ->
            if (relay.startsWith("wss://")) {
                connected += relay
            }
        }
    }

    fun connectedRelays(): Set<String> = connected
}
