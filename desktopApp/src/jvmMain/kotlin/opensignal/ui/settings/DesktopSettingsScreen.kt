package opensignal.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.io.File
import opensignal.settings.BlossomServerPresets
import opensignal.settings.ThemeMode
import opensignal.settings.UserSettings
import opensignal.nostr.relay_manager.RelayConfig
import opensignal.ui.components.RelayListEditor
import opensignal.ui.components.RelayStatusIndicator

@Composable
fun DesktopSettingsScreen(
    settings: UserSettings,
    onUpdateSettings: ((UserSettings) -> UserSettings) -> Unit,
    pickFile: (String) -> File?
) {
    var localError by remember { mutableStateOf<String?>(null) }
    var blossomServer by remember(settings.blossomServer) { mutableStateOf(settings.blossomServer) }
    
    // Convert string list to RelayConfig list
    var relayConfigs by remember(settings.preferredRelays) {
        mutableStateOf(settings.preferredRelays.map { RelayConfig(url = it) })
    }
    
    // Track connectivity status of relays (simulated - in production would connect to relays)
    var connectedRelays by remember { mutableStateOf<Set<String>>(emptySet()) }
    var isRefreshingRelays by remember { mutableStateOf(false) }

    Card(modifier = Modifier.fillMaxWidth().fillMaxHeight()) {
        Column(
            modifier = Modifier
                .padding(12.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Settings")
            
            // === Blossom Configuration ===
            Text("Blossom NIP-96 Media Server", style = androidx.compose.material3.MaterialTheme.typography.titleMedium)
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
            
            Divider(modifier = Modifier.padding(vertical = 8.dp))
            
            // === Relay Management (NIP-65) ===
            Text("Relay Management (NIP-65)", style = androidx.compose.material3.MaterialTheme.typography.titleMedium)
            Text(
                "Configure Nostr relays for publishing signals. These relays will receive your trade signals.",
                style = androidx.compose.material3.MaterialTheme.typography.bodySmall
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Relay Configuration (NIP-65)", style = androidx.compose.material3.MaterialTheme.typography.titleMedium)
                    Text(
                        "${relayConfigs.size} relays • ${connectedRelays.size} connected",
                        style = androidx.compose.material3.MaterialTheme.typography.labelSmall
                    )
                }
                IconButton(onClick = {
                    isRefreshingRelays = true
                    // Simulate refreshing relays from NIP-65 event
                    connectedRelays = relayConfigs.map { it.url }.shuffled().take(
                        (relayConfigs.size / 2) + 1
                    ).toSet() // Simulate partial connectivity
                    isRefreshingRelays = false
                }) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Refresh relays from NIP-65"
                    )
                }
            }
            
            RelayStatusIndicator(
                relays = relayConfigs.map { it.url },
                readableCount = relayConfigs.count { it.canRead() },
                writeableCount = relayConfigs.count { it.canWrite() },
                modifier = Modifier.padding(vertical = 8.dp)
            )
            
            RelayListEditor(
                relays = relayConfigs,
                onRelaysChanged = { updated ->
                    relayConfigs = updated
                    onUpdateSettings {
                        it.copy(preferredRelays = updated.map { relay -> relay.url })
                    }
                },
                readOnly = false,
                connectedRelays = connectedRelays
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
                Text("Show liquidity overlays")
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Checkbox(
                    checked = settings.showStructureBreaks,
                    onCheckedChange = { checked ->
                        onUpdateSettings { it.copy(showStructureBreaks = checked) }
                    }
                )
                Text("Show structure overlays")
            }
            
            Divider(modifier = Modifier.padding(vertical = 8.dp))
            
            // === Model Files ===
            Text("AI Model Files", style = androidx.compose.material3.MaterialTheme.typography.titleMedium)
            Text("Best practice: ONNX, NCHW float32. Inputs: 640x640 for candle/liquidity/structure, 224x224 for trend.", style = androidx.compose.material3.MaterialTheme.typography.labelSmall)
            Text("Recommended families: YOLO/YOLOX for detection, EfficientNet/ResNet for trend.", style = androidx.compose.material3.MaterialTheme.typography.labelSmall)
            
            Button(onClick = {
                val file = pickFile("Select Candle Model")
                if (file != null && file.exists() && file.name.endsWith(".onnx", ignoreCase = true)) {
                    onUpdateSettings { it.copy(candleModelPath = file.absolutePath) }
                } else if (file != null) {
                    localError = "Please select a .onnx model file."
                }
            }) { Text("Select Candle Model") }
            Text("Candle: ${shortPath(settings.candleModelPath)}")
            
            Button(onClick = {
                val file = pickFile("Select Liquidity Model")
                if (file != null && file.exists() && file.name.endsWith(".onnx", ignoreCase = true)) {
                    onUpdateSettings { it.copy(liquidityModelPath = file.absolutePath) }
                } else if (file != null) {
                    localError = "Please select a .onnx model file."
                }
            }) { Text("Select Liquidity Model") }
            Text("Liquidity: ${shortPath(settings.liquidityModelPath)}")
            
            Button(onClick = {
                val file = pickFile("Select Structure Model")
                if (file != null && file.exists() && file.name.endsWith(".onnx", ignoreCase = true)) {
                    onUpdateSettings { it.copy(structureModelPath = file.absolutePath) }
                } else if (file != null) {
                    localError = "Please select a .onnx model file."
                }
            }) { Text("Select Structure Model") }
            Text("Structure: ${shortPath(settings.structureModelPath)}")
            
            Button(onClick = {
                val file = pickFile("Select Trend Model")
                if (file != null && file.exists() && file.name.endsWith(".onnx", ignoreCase = true)) {
                    onUpdateSettings { it.copy(trendModelPath = file.absolutePath) }
                } else if (file != null) {
                    localError = "Please select a .onnx model file."
                }
            }) { Text("Select Trend Model") }
            Text("Trend: ${shortPath(settings.trendModelPath)}")
            
            Divider(modifier = Modifier.padding(vertical = 8.dp))
            
            // === Theme ===
            Text("Appearance", style = androidx.compose.material3.MaterialTheme.typography.titleMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = {
                    onUpdateSettings {
                        val nextTheme = when (it.themeMode) {
                            ThemeMode.SYSTEM -> ThemeMode.LIGHT
                            ThemeMode.LIGHT -> ThemeMode.DARK
                            ThemeMode.DARK -> ThemeMode.HIGH_CONTRAST
                            ThemeMode.HIGH_CONTRAST -> ThemeMode.SYSTEM
                        }
                        it.copy(themeMode = nextTheme)
                    }
                }) {
                    Text("Theme: ${settings.themeMode}")
                }
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
