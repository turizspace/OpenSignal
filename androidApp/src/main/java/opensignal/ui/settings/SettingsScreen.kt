package opensignal.ui.settings

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.io.File
import opensignal.settings.BlossomServerPresets
import opensignal.settings.ThemeMode
import opensignal.settings.UserSettings
import opensignal.nostr.relay_manager.RelayConfig
import opensignal.ui.components.AndroidRelayListEditor
import opensignal.ui.components.AndroidRelayStatusIndicator

@Composable
fun SettingsScreen(
    settings: UserSettings,
    onUpdateSettings: ((UserSettings) -> UserSettings) -> Unit,
    copyModelToAppStorage: (Uri, String) -> String?,
    queryDisplayName: (Uri) -> String?
) {
    var localError by remember { mutableStateOf<String?>(null) }
    var pendingModelTarget by remember { mutableStateOf<ModelTarget?>(null) }
    var blossomServer by remember(settings.blossomServer) { mutableStateOf(settings.blossomServer) }
    
    // Convert string list to RelayConfig list
    var relayConfigs by remember(settings.preferredRelays) {
        mutableStateOf(settings.preferredRelays.map { RelayConfig(url = it) })
    }
    
    // Track connectivity status of relays (simulated - in production would connect to relays)
    var connectedRelays by remember { mutableStateOf<Set<String>>(emptySet()) }
    var isRefreshingRelays by remember { mutableStateOf(false) }
    
    val modelPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        val target = pendingModelTarget
        pendingModelTarget = null
        if (uri == null || target == null) return@rememberLauncherForActivityResult
        val displayName = queryDisplayName(uri) ?: target.fileName
        if (!displayName.endsWith(".onnx", ignoreCase = true)) {
            localError = "Please select a .onnx model file."
            return@rememberLauncherForActivityResult
        }
        val storedPath = copyModelToAppStorage(uri, target.fileName)
        if (storedPath.isNullOrBlank()) {
            localError = "Unable to import model file."
            return@rememberLauncherForActivityResult
        }
        onUpdateSettings {
            when (target) {
                ModelTarget.CANDLE -> it.copy(candleModelPath = storedPath)
                ModelTarget.LIQUIDITY -> it.copy(liquidityModelPath = storedPath)
                ModelTarget.STRUCTURE -> it.copy(structureModelPath = storedPath)
                ModelTarget.TREND -> it.copy(trendModelPath = storedPath)
            }
        }
        localError = null
    }

    Card(modifier = Modifier.fillMaxWidth().fillMaxHeight()) {
        Column(
            modifier = Modifier
                .padding(10.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // === Blossom Configuration ===
            Text("Blossom NIP-96 Server", style = androidx.compose.material3.MaterialTheme.typography.titleMedium)
            OutlinedTextField(
                value = blossomServer,
                onValueChange = { blossomServer = it },
                label = { Text("Blossom server") },
                modifier = Modifier.fillMaxWidth()
            )
            Text("Blossom presets", style = androidx.compose.material3.MaterialTheme.typography.labelMedium)
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                BlossomServerPresets.forEach { preset ->
                    Button(
                        onClick = {
                            blossomServer = preset.baseUrl
                            onUpdateSettings { it.copy(blossomServer = preset.baseUrl) }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("${preset.name} • ${preset.baseUrl}")
                    }
                }
            }
            
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            
            // === Relay Management (NIP-65) ===
            Text("Relay Management (NIP-65)", style = androidx.compose.material3.MaterialTheme.typography.titleMedium)
            
            AndroidRelayStatusIndicator(
                relays = relayConfigs.map { it.url },
                modifier = Modifier.padding(vertical = 8.dp)
            )
            
            AndroidRelayListEditor(
                relays = relayConfigs,
                onRelaysChanged = { updated ->
                    relayConfigs = updated
                    onUpdateSettings {
                        it.copy(preferredRelays = updated.map { relay -> relay.url })
                    }
                },
                readOnly = false,
                maxRelays = 5,
                connectedRelays = connectedRelays,
                onRefreshRelays = {
                    isRefreshingRelays = true
                    // Simulate refreshing relays from NIP-65 event
                    // In production: would call nostrClient.fetchUserRelayPreferences()
                    connectedRelays = relayConfigs.map { it.url }.shuffled().take(
                        (relayConfigs.size / 2) + 1
                    ).toSet()  // Simulate partial connectivity
                    isRefreshingRelays = false
                }
            )
            
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            
            // === Chart Display Options ===
            Text("Chart Display", style = androidx.compose.material3.MaterialTheme.typography.titleMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Checkbox(
                    checked = settings.showLiquidityZones,
                    onCheckedChange = { checked ->
                        onUpdateSettings { it.copy(showLiquidityZones = checked) }
                    }
                )
                Text("Show liquidity")
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Checkbox(
                    checked = settings.showStructureBreaks,
                    onCheckedChange = { checked ->
                        onUpdateSettings { it.copy(showStructureBreaks = checked) }
                    }
                )
                Text("Show structure")
            }
            
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            
            // === Model Files ===
            Text("AI Model Files", style = androidx.compose.material3.MaterialTheme.typography.titleMedium)
            Text("Best practice: ONNX, NCHW float32. Inputs: 640x640 for candle/liquidity/structure, 224x224 for trend.", style = androidx.compose.material3.MaterialTheme.typography.labelSmall)
            Text("Recommended families: YOLO (candle/liquidity/structure) + EfficientNet/MobileNetV3 (trend).", style = androidx.compose.material3.MaterialTheme.typography.labelSmall)
            
            Button(onClick = {
                pendingModelTarget = ModelTarget.CANDLE
                modelPicker.launch("*/*")
            }) { Text("Select Candle Model") }
            Text("Candle: ${shortPath(settings.candleModelPath)}")
            
            Button(onClick = {
                pendingModelTarget = ModelTarget.LIQUIDITY
                modelPicker.launch("*/*")
            }) { Text("Select Liquidity Model") }
            Text("Liquidity: ${shortPath(settings.liquidityModelPath)}")
            
            Button(onClick = {
                pendingModelTarget = ModelTarget.STRUCTURE
                modelPicker.launch("*/*")
            }) { Text("Select Structure Model") }
            Text("Structure: ${shortPath(settings.structureModelPath)}")
            
            Button(onClick = {
                pendingModelTarget = ModelTarget.TREND
                modelPicker.launch("*/*")
            }) { Text("Select Trend Model") }
            Text("Trend: ${shortPath(settings.trendModelPath)}")
            
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            
            // === Theme ===
            Text("Appearance", style = androidx.compose.material3.MaterialTheme.typography.titleMedium)
            Button(onClick = {
                onUpdateSettings {
                    val nextTheme = when (it.themeMode) {
                        ThemeMode.SYSTEM -> ThemeMode.LIGHT
                        ThemeMode.LIGHT -> ThemeMode.DARK
                        ThemeMode.DARK -> ThemeMode.HIGH_CONTRAST
                        ThemeMode.HIGH_CONTRAST -> ThemeMode.SYSTEM
                    }
                    it.copy(themeMode = nextTheme, blossomServer = blossomServer)
                }
            }) {
                Text("Theme: ${settings.themeMode}")
            }
            
            // Error display
            localError?.let { 
                Text("Error: $it", color = androidx.compose.material3.MaterialTheme.colorScheme.error)
            }
        }
    }
}

private fun shortPath(path: String): String {
    if (path.isBlank()) return "Not set"
    return File(path).name.ifBlank { path }
}

enum class ModelTarget(val modelName: String, val fileName: String) {
    CANDLE("candle_detector", "candle_detector.onnx"),
    LIQUIDITY("liquidity_sweep", "liquidity_sweep.onnx"),
    STRUCTURE("structure_detector", "structure_detector.onnx"),
    TREND("trend_classifier", "trend_classifier.onnx")
}
