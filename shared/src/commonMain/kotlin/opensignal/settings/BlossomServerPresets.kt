package opensignal.settings

data class BlossomServerPreset(
    val name: String,
    val baseUrl: String
)

val BlossomServerPresets = listOf(
    BlossomServerPreset("Primal", "https://blossom.primal.net"),
    BlossomServerPreset("Azzamo", "https://blossom.azzamo.media"),
    BlossomServerPreset("NostrMedia", "https://nostrmedia.com"),
    BlossomServerPreset("24242", "https://24242.io"),
    BlossomServerPreset("nostr.build", "https://nostr.build")
)
