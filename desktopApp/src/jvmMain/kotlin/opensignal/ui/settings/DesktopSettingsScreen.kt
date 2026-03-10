package opensignal.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
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

@Composable
fun DesktopSettingsScreen(
    settings: UserSettings,
    onUpdateSettings: ((UserSettings) -> UserSettings) -> Unit,
    pickFile: (String) -> File?
) {
    var localError by remember { mutableStateOf<String?>(null) }
    var blossomServer by remember(settings.blossomServer) { mutableStateOf(settings.blossomServer) }
    var relayInput by remember(settings.preferredRelays) {
        mutableStateOf(settings.preferredRelays.joinToString(","))
    }

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Settings")
            OutlinedTextField(
                value = blossomServer,
                onValueChange = { blossomServer = it },
                label = { Text("Blossom NIP-96 server") },
                modifier = Modifier.fillMaxWidth()
            )
            Text("Blossom presets")
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
            OutlinedTextField(
                value = relayInput,
                onValueChange = { relayInput = it },
                label = { Text("Relays (comma separated)") },
                modifier = Modifier.fillMaxWidth()
            )
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
            Text("Model Files")
            Text("Best practice: ONNX, NCHW float32. Inputs: 640x640 for candle/liquidity/structure, 224x224 for trend.")
            Text("Recommended families: YOLO/YOLOX for detection, EfficientNet/ResNet for trend.")
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

                Button(onClick = {
                    onUpdateSettings {
                        it.copy(
                            blossomServer = blossomServer,
                            preferredRelays = relayInput.split(',').map(String::trim).filter(String::isNotBlank)
                        )
                    }
                }) {
                    Text("Save Settings")
                }
            }

            localError?.let { Text("Error: $it") }
        }
    }
}

private fun shortPath(path: String): String {
    if (path.isBlank()) return "Not set"
    return File(path).name.ifBlank { path }
}
