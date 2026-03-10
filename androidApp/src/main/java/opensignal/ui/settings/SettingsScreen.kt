package opensignal.ui.settings

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
fun SettingsScreen(
    settings: UserSettings,
    onUpdateSettings: ((UserSettings) -> UserSettings) -> Unit,
    copyModelToAppStorage: (Uri, String) -> String?,
    queryDisplayName: (Uri) -> String?
) {
    var localError by remember { mutableStateOf<String?>(null) }
    var pendingModelTarget by remember { mutableStateOf<ModelTarget?>(null) }
    var blossomServer by remember(settings.blossomServer) { mutableStateOf(settings.blossomServer) }
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

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = blossomServer,
                onValueChange = { blossomServer = it },
                label = { Text("Blossom server") },
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
            Button(onClick = {
                onUpdateSettings { it.copy(blossomServer = blossomServer) }
            }) {
                Text("Save server")
            }
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
            Text("Model Files")
            Text("Best practice: ONNX, NCHW float32. Inputs: 640x640 for candle/liquidity/structure, 224x224 for trend.")
            Text("Recommended families: YOLO (candle/liquidity/structure) + EfficientNet/MobileNetV3 (trend).")
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
            localError?.let { Text("Error: $it") }
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
