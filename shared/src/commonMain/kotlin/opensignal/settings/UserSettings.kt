package opensignal.settings

import kotlinx.serialization.Serializable

@Serializable
enum class ThemeMode {
    SYSTEM,
    LIGHT,
    DARK,
    HIGH_CONTRAST
}

@Serializable
enum class UiDensity {
    COMPACT,
    COMFORTABLE,
    EXPANDED
}

@Serializable
data class UserSettings(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val uiDensity: UiDensity = UiDensity.COMFORTABLE,
    val showLiquidityZones: Boolean = true,
    val showStructureBreaks: Boolean = true,
    val showFundamentalPanel: Boolean = true,
    val preferredRelays: List<String> = listOf(
        "wss://relay.damus.io",
        "wss://relay.primal.net"
    ),
    val blossomServer: String = BlossomServerPresets.first().baseUrl,
    val aiServiceEndpoint: String = "http://localhost:8000/analyze",
    val candleModelPath: String = "",
    val liquidityModelPath: String = "",
    val structureModelPath: String = "",
    val trendModelPath: String = "",
    val chartGridOpacity: Float = 0.2f,
    val chartFontScale: Float = 1.0f
)
