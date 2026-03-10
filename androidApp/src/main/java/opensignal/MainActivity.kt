package opensignal

import android.os.Bundle
import android.provider.OpenableColumns
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.Typography
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import java.io.File
import java.util.Locale
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.OffsetDateTime
import kotlinx.coroutines.launch
import opensignal.auth.SecureAuthStore
import opensignal.ai.model_loader.ModelRegistry
import opensignal.ai.pipeline.OpenSignalVisionAnalyzer
import opensignal.ai.pipeline.RuleBasedFundamentalAnalyzer
import opensignal.blossom.nip96_client.Nip96ServerConfig
import opensignal.blossom.upload_service.BlossomUploadService
import opensignal.charts.TradingChart
import opensignal.domain.AnalyzeScreenshotUseCase
import opensignal.domain.CopilotUiState
import opensignal.domain.OpenSignalController
import opensignal.domain.ScreenshotUploader
import opensignal.domain.parseTimeframeOrDefault
import opensignal.models.LoginMethod
import opensignal.models.ScreenshotPayload
import opensignal.models.TradeOption
import opensignal.models.TradeSide
import opensignal.models.TrendDirection
import opensignal.nostr.NostrClient
import opensignal.nostr.AmberSignerManager
import opensignal.settings.InMemorySettingsRepository
import opensignal.settings.SettingsRepository
import opensignal.settings.ThemeMode
import opensignal.settings.UserSettings
import opensignal.ui.login.ExternalSignerLoginCard
import opensignal.ui.login.NsecLoginCard
import opensignal.android.R
import opensignal.ui.settings.ModelTarget
import opensignal.ui.settings.SettingsScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        intent?.let { AmberSignerManager.handleIntentResponse(it) }

        setContent {
            AndroidAppShell()
        }
    }

    override fun onNewIntent(intent: android.content.Intent) {
        super.onNewIntent(intent)
        AmberSignerManager.handleIntentResponse(intent)
    }
}

private fun readBytesFromUri(context: android.content.Context, uri: android.net.Uri): ByteArray? {
    return context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
}

private fun copyModelToAppStorage(
    context: android.content.Context,
    uri: android.net.Uri,
    targetName: String
): String? {
    val modelsDir = File(context.filesDir, "models").apply { mkdirs() }
    val targetFile = File(modelsDir, targetName)
    return try {
        context.contentResolver.openInputStream(uri)?.use { input ->
            targetFile.outputStream().use { output -> input.copyTo(output) }
        } ?: return null
        targetFile.absolutePath
    } catch (_: Exception) {
        null
    }
}

private fun queryDisplayName(context: android.content.Context, uri: android.net.Uri): String? {
    val resolver = context.contentResolver
    return resolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)
        ?.use { cursor ->
            if (!cursor.moveToFirst()) return@use null
            val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (index >= 0) cursor.getString(index) else null
        }
}

private fun updateModelOverrides(target: MutableMap<String, String>, settings: UserSettings) {
    target.clear()
    if (settings.candleModelPath.isNotBlank()) {
        target[ModelTarget.CANDLE.modelName] = settings.candleModelPath
    }
    if (settings.liquidityModelPath.isNotBlank()) {
        target[ModelTarget.LIQUIDITY.modelName] = settings.liquidityModelPath
    }
    if (settings.structureModelPath.isNotBlank()) {
        target[ModelTarget.STRUCTURE.modelName] = settings.structureModelPath
    }
    if (settings.trendModelPath.isNotBlank()) {
        target[ModelTarget.TREND.modelName] = settings.trendModelPath
    }
}

private enum class AndroidRoute {
    LOGIN,
    UPLOAD,
    SETTINGS
}

private const val ANALYSIS_SYSTEM_PROMPT =
    "Upload screenshot data for AI model to generate technical + fundamental predictions backed by news."

private val LuxuryGold = Color(0xFFC9A227)
private val LuxuryPanel = Color(0xFF141820)
private val LuxuryPanelAlt = Color(0xFF1B2029)
private val LuxuryBorder = Color(0xFF2A3240)
private val Bullish = Color(0xFF2E7D32)
private val Bearish = Color(0xFFC62828)
private val Neutral = Color(0xFFB08900)

@Composable
private fun isDarkTheme(): Boolean = MaterialTheme.colorScheme.background.luminance() < 0.5f

@Composable
private fun luxuryPanelColor(): Color =
    if (isDarkTheme()) LuxuryPanel else MaterialTheme.colorScheme.surface

@Composable
private fun luxuryPanelAltColor(): Color =
    if (isDarkTheme()) LuxuryPanelAlt else MaterialTheme.colorScheme.surfaceVariant

@Composable
private fun luxuryBorderColor(): Color =
    if (isDarkTheme()) LuxuryBorder else MaterialTheme.colorScheme.outline

@Composable
private fun mutedTextColor(): Color =
    if (isDarkTheme()) Color.LightGray else MaterialTheme.colorScheme.onSurfaceVariant

private class SettingsAwareUploader(
    private val settingsRepository: SettingsRepository
) : ScreenshotUploader {
    override suspend fun upload(payload: ScreenshotPayload) = BlossomUploadService(
        Nip96ServerConfig(baseUrl = settingsRepository.settings.value.blossomServer)
    ).upload(payload)
}

@Composable
private fun AndroidAppShell() {
    val context = LocalContext.current
    val authStore = remember(context) { SecureAuthStore(context) }
    val settingsRepository = remember { InMemorySettingsRepository() }
    val nostrClient = remember { NostrClient() }
    val screenshotUploader = remember { SettingsAwareUploader(settingsRepository) }
    val fundamental = remember { RuleBasedFundamentalAnalyzer() }
    val settings by settingsRepository.settings.collectAsState()
    val modelOverrides = remember { mutableMapOf<String, String>() }
    LaunchedEffect(
        settings.candleModelPath,
        settings.liquidityModelPath,
        settings.structureModelPath,
        settings.trendModelPath
    ) {
        updateModelOverrides(modelOverrides, settings)
    }
    val registry = remember { ModelRegistry(overrides = modelOverrides) }
    val analyzer = remember { OpenSignalVisionAnalyzer(registry = registry) }
    val useCase = remember {
        AnalyzeScreenshotUseCase(
            screenshotUploader = screenshotUploader,
            chartVisionAnalyzer = analyzer,
            fundamentalAnalyzer = fundamental,
            signalPublisher = nostrClient
        )
    }
    val controller = remember {
        OpenSignalController(
            authGateway = nostrClient,
            analyzeUseCase = useCase,
            settingsRepository = settingsRepository
        )
    }
    val isDark = when (settings.themeMode) {
        ThemeMode.DARK, ThemeMode.HIGH_CONTRAST -> true
        ThemeMode.LIGHT -> false
        ThemeMode.SYSTEM -> androidx.compose.foundation.isSystemInDarkTheme()
    }
    val colorScheme = luxuryColorScheme(isDark)
    val typography = luxuryTypography()

    MaterialTheme(colorScheme = colorScheme, typography = typography) {
        LuxuryShell {
            AndroidCopilotScreen(
                controller = controller,
                settingsRepository = settingsRepository,
                authStore = authStore
            )
        }
    }
}

@Composable
private fun AndroidCopilotScreen(
    controller: OpenSignalController,
    settingsRepository: SettingsRepository,
    authStore: SecureAuthStore
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val state by controller.state.collectAsState()
    val settings by settingsRepository.settings.collectAsState()
    var route by remember { mutableStateOf(AndroidRoute.LOGIN) }
    var didAttemptRestore by remember { mutableStateOf(false) }
    val intentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        result.data?.let { AmberSignerManager.handleIntentResponse(it) }
    }

    DisposableEffect(Unit) {
        val launcher: (android.content.Intent) -> Unit = { intent -> intentLauncher.launch(intent) }
        AmberSignerManager.registerActivityLauncher(launcher)
        onDispose { AmberSignerManager.unregisterActivityLauncher(launcher) }
    }

    LaunchedEffect(state.authSession) {
        route = if (state.authSession == null) AndroidRoute.LOGIN else AndroidRoute.UPLOAD
    }

    LaunchedEffect(Unit) {
        val stored = authStore.load()
        if (stored != null && controller.state.value.authSession == null) {
            when (stored.method) {
                LoginMethod.NSEC -> {
                    controller.loginWithNsec(stored.nsec.orEmpty(), stored.relayHint)
                }
                LoginMethod.EXTERNAL_SIGNER -> {
                    val pkg = stored.externalPackage.orEmpty()
                    AmberSignerManager.configure(stored.pubkey, pkg)
                    controller.loginWithExternalSignerSession(
                        pubkey = stored.pubkey,
                        packageName = pkg,
                        relayHint = stored.relayHint
                    )
                }
            }
        }
        didAttemptRestore = true
    }

    LaunchedEffect(state.authSession, didAttemptRestore) {
        if (didAttemptRestore && state.authSession == null) {
            authStore.clear()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        TopBar(
            title = when (route) {
                AndroidRoute.SETTINGS -> "Settings"
                else -> "OpenSignal"
            },
            showSettings = route == AndroidRoute.UPLOAD,
            onSettings = { route = AndroidRoute.SETTINGS },
            showBack = route == AndroidRoute.SETTINGS,
            onBack = { route = AndroidRoute.UPLOAD }
        )

        when (route) {
            AndroidRoute.LOGIN -> NostrLoginScreen(
                state = state,
                onNsecLogin = { nsec, relayHint ->
                    scope.launch {
                        controller.loginWithNsec(nsec, relayHint)
                        controller.state.value.authSession?.let { session ->
                            authStore.save(session = session, nsec = nsec, externalPackage = null)
                        }
                    }
                },
                onExternalSignerLogin = { signerInput ->
                    scope.launch {
                        val packageName = signerInput.takeIf { it.startsWith("com.") }
                            ?: "com.greenart7c3.nostrsigner"
                        try {
                            val result = AmberSignerManager.requestPublicKey(packageName = packageName)
                            val pubkey = result.pubkey ?: result.result ?: result.event
                            if (pubkey.isNullOrBlank()) {
                                controller.reportError("External signer did not return a public key")
                                return@launch
                            }
                            val resolvedPackage = result.packageName ?: packageName
                            AmberSignerManager.configure(pubkey, resolvedPackage)
                            controller.loginWithExternalSignerSession(
                                pubkey = pubkey,
                                packageName = resolvedPackage,
                                relayHint = settings.preferredRelays.firstOrNull()
                            )
                            controller.state.value.authSession?.let { session ->
                                authStore.save(
                                    session = session,
                                    nsec = null,
                                    externalPackage = resolvedPackage
                                )
                            }
                            route = AndroidRoute.UPLOAD
                        } catch (error: Throwable) {
                            controller.reportError(error.message ?: "External signer failed")
                        }
                    }
                },
                relayHint = settings.preferredRelays.firstOrNull()
            )

            AndroidRoute.UPLOAD -> UploadScreenshotPredictionScreen(
                state = state,
                settings = settings,
                onUploadAndPredict = { bytes, fileName, mimeType ->
                    scope.launch {
                        controller.uploadScreenshotForPrediction(
                            symbol = "AUTO",
                            timeframe = parseTimeframeOrDefault("H1"),
                            screenshot = ScreenshotPayload(
                                name = fileName,
                                mimeType = mimeType,
                                bytes = bytes
                            ),
                            accountBalance = 10_000.0,
                            riskPercent = 1.0,
                            leverage = 3.0,
                            userContext = ANALYSIS_SYSTEM_PROMPT
                        )
                    }
                },
                onUpdateSettings = { update ->
                    scope.launch { settingsRepository.update(update) }
                }
            )

            AndroidRoute.SETTINGS -> SettingsScreen(
                settings = settings,
                onUpdateSettings = { update ->
                    scope.launch { settingsRepository.update(update) }
                },
                copyModelToAppStorage = { uri, targetName ->
                    copyModelToAppStorage(context, uri, targetName)
                },
                queryDisplayName = { uri ->
                    queryDisplayName(context, uri)
                }
            )
        }
    }
}

@Composable
private fun TopBar(
    title: String,
    showSettings: Boolean,
    onSettings: () -> Unit,
    showBack: Boolean,
    onBack: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (showBack) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.Filled.ArrowBack,
                    contentDescription = "Back"
                )
            }
        } else {
            Spacer(modifier = Modifier.size(48.dp))
        }

        Text(
            title,
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.weight(1f)
        )

        if (showSettings) {
            IconButton(onClick = onSettings) {
                Icon(
                    imageVector = Icons.Filled.Settings,
                    contentDescription = "Settings"
                )
            }
        } else {
            Spacer(modifier = Modifier.size(48.dp))
        }
    }
}

@Composable
private fun NostrLoginScreen(
    state: CopilotUiState,
    relayHint: String?,
    onNsecLogin: (String, String?) -> Unit,
    onExternalSignerLogin: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("Nostr Login", style = MaterialTheme.typography.titleMedium)
        Text("Sign in first to continue to screenshot upload and model predictions.")
        NsecLoginCard(onLogin = { nsec -> onNsecLogin(nsec, relayHint) })
        ExternalSignerLoginCard(onLogin = onExternalSignerLogin)
        Text("Session: ${state.authSession?.pubkey ?: "Not authenticated"}")
        state.error?.let { Text("Error: $it") }
    }
}

@Composable
private fun UploadScreenshotPredictionScreen(
    state: CopilotUiState,
    settings: UserSettings,
    onUploadAndPredict: (
        bytes: ByteArray,
        fileName: String,
        mimeType: String
    ) -> Unit,
    onUpdateSettings: ((UserSettings) -> UserSettings) -> Unit
) {
    val context = LocalContext.current
    var selectedBytes by remember { mutableStateOf<ByteArray?>(null) }
    var selectedName by remember { mutableStateOf<String?>(null) }
    var selectedMime by remember { mutableStateOf<String?>(null) }
    var localError by remember { mutableStateOf<String?>(null) }
    val pickImageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        val bytes = readBytesFromUri(context, uri)
        if (bytes == null) {
            localError = "Unable to read the selected screenshot."
            return@rememberLauncherForActivityResult
        }
        selectedBytes = bytes
        selectedMime = context.contentResolver.getType(uri) ?: "image/png"
        selectedName = queryDisplayName(context, uri) ?: "screenshot.png"
        localError = null
    }

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(ANALYSIS_SYSTEM_PROMPT)

            Button(onClick = { pickImageLauncher.launch("image/*") }) {
                Text("Select Screenshot")
            }

            Text("Selected: ${selectedName ?: "None"}")

            Button(onClick = {
                val bytes = selectedBytes ?: return@Button
                val name = selectedName ?: "screenshot.png"
                val mime = selectedMime ?: "image/png"
                onUploadAndPredict(bytes, name, mime)
            }, enabled = selectedBytes != null && !state.isLoading) {
                Text(if (state.isLoading) "Analyzing..." else "Analyze Screenshot")
            }

            localError?.let { Text("Error: $it") }
            state.error?.let { Text("Error: $it") }
        }
    }

    state.latestAnalysis?.let { analysis ->
        AnalysisPanel(
            analysis = analysis,
            settings = settings
        )
    }
}

@Composable
private fun AnalysisPanel(
    analysis: opensignal.models.CopilotAnalysis,
    settings: UserSettings
) {
    val technical = analysis.signal.technical
    val fundamental = analysis.signal.fundamental
    val plan = analysis.signal.tradePlan
    val trendColor = when (technical.trend) {
        TrendDirection.BULLISH -> Bullish
        TrendDirection.BEARISH -> Bearish
        TrendDirection.SIDEWAYS -> Neutral
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = luxuryPanelColor())
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            SectionHeader(
                title = "Signal Overview",
                subtitle = formatTimestamp(analysis.signal.generatedAtIso),
                accent = LuxuryGold
            )
            StatPillRow(
                items = listOf(
                    "Trend" to technical.trend.name,
                    "Confidence" to formatPercent(analysis.signal.confidence),
                    "Timeframe" to technical.timeframe.name,
                    "Price" to formatPrice(technical.currentPrice)
                ),
                trendColor = trendColor
            )
            TradingChart(
                technical = technical,
                tradePlan = plan,
                showLiquidity = settings.showLiquidityZones,
                showStructure = settings.showStructureBreaks,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }

    Spacer(modifier = Modifier.height(10.dp))

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = luxuryPanelAltColor())
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            SectionHeader(title = "Trade Plan", subtitle = "", accent = LuxuryGold)
            TradeOptionList(title = "Buy Options", options = plan.buyOptions)
            Divider(color = luxuryBorderColor())
            TradeOptionList(title = "Sell Options", options = plan.sellOptions)
        }
    }

    Spacer(modifier = Modifier.height(10.dp))

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = luxuryPanelColor())
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            SectionHeader(
                title = "Technical Summary",
                subtitle = technical.summary,
                accent = LuxuryGold,
                maxLines = 3
            )
            Text("Structure events: ${technical.structureEvents.size}")
            Text("Liquidity sweeps: ${technical.liquiditySweeps.size}")
            Text("Support/Resistance: ${technical.supportResistance.size}")
        }
    }

    Spacer(modifier = Modifier.height(10.dp))

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = luxuryPanelAltColor())
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            SectionHeader(
                title = "Fundamental Summary",
                subtitle = fundamental.summary,
                accent = LuxuryGold,
                maxLines = 3
            )
            Text("Overall bias: ${fundamental.overallBias.name}")
            Text("Score: ${formatPercent(fundamental.score)}")
            if (fundamental.riskFlags.isNotEmpty()) {
                Text("Risk flags:")
                fundamental.riskFlags.forEach { flag ->
                    Text(
                        "- $flag",
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }

    analysis.publishedSignal?.let { published ->
        Spacer(modifier = Modifier.height(10.dp))
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = luxuryPanelColor(),
            shape = MaterialTheme.shapes.medium
        ) {
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                SectionHeader(title = "Nostr Publish", subtitle = "Event ${published.eventId}", accent = LuxuryGold)
                Text(
                    "Relays: ${published.relays.joinToString(", ")}",
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text("Published at: ${published.publishedAtIso}")
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String, subtitle: String?, accent: Color, maxLines: Int = 2) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            CandleIcon(color = accent, modifier = Modifier)
            Text(
                title,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        if (!subtitle.isNullOrBlank()) {
            Text(
                subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = mutedTextColor(),
                maxLines = maxLines,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun StatPill(label: String, value: String, accent: Color = LuxuryGold) {
    Surface(
        color = luxuryPanelAltColor(),
        shape = MaterialTheme.shapes.small
    ) {
        Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)) {
            Text(label, style = MaterialTheme.typography.labelSmall, color = mutedTextColor())
            Text(
                value,
                style = MaterialTheme.typography.bodyMedium,
                color = accent,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun TradeOptionList(title: String, options: List<TradeOption>) {
    val accent = if (title.contains("Buy", ignoreCase = true)) Bullish else Bearish
    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            ArrowIcon(color = accent, up = title.contains("Buy", ignoreCase = true))
            Text(title, style = MaterialTheme.typography.titleSmall, color = accent)
        }
        if (options.isEmpty()) {
            Text("No options available")
        } else {
            options.forEach { option ->
                TradeOptionRow(option = option)
            }
        }
    }
}

@Composable
private fun TradeOptionRow(option: TradeOption) {
    val accent = if (option.side == TradeSide.BUY) Bullish else Bearish
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = luxuryPanelColor(),
        shape = MaterialTheme.shapes.small
    ) {
        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = "${option.side.name} | Entry ${formatPrice(option.entry)} | SL ${formatPrice(option.stopLoss)} | TP ${formatPrice(option.takeProfit)}",
                style = MaterialTheme.typography.bodySmall,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.fillMaxWidth()
            )
            StatPillRow(
                items = listOf(
                    "RR" to formatNumber(option.riskReward),
                    "Size" to formatNumber(option.positionSizeUnits),
                    "Risk" to formatPrice(option.capitalAtRisk)
                ),
                trendColor = accent
            )
            if (option.note.isNotBlank()) {
                Text(
                    option.note,
                    style = MaterialTheme.typography.bodySmall,
                    color = mutedTextColor(),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun StatPillRow(items: List<Pair<String, String>>, trendColor: Color) {
    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items.forEach { (label, value) ->
            val accent = if (label == "Trend") trendColor else LuxuryGold
            StatPill(label = label, value = value, accent = accent)
        }
    }
}

@Composable
private fun CandleIcon(color: Color, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.padding(top = 2.dp).size(width = 18.dp, height = 22.dp)) {
        val w = size.width
        val h = size.height
        val centerX = w / 2f
        drawLine(
            color = color,
            start = androidx.compose.ui.geometry.Offset(centerX, h * 0.1f),
            end = androidx.compose.ui.geometry.Offset(centerX, h * 0.9f),
            strokeWidth = 2f,
            cap = StrokeCap.Round
        )
        drawRect(
            color = color,
            topLeft = androidx.compose.ui.geometry.Offset(centerX - 3f, h * 0.35f),
            size = androidx.compose.ui.geometry.Size(6f, h * 0.3f)
        )
    }
}

@Composable
private fun ArrowIcon(color: Color, up: Boolean) {
    Canvas(modifier = Modifier.padding(top = 2.dp).size(14.dp)) {
        val w = size.width
        val h = size.height
        val path = Path().apply {
            if (up) {
                moveTo(w / 2f, 0f)
                lineTo(w, h)
                lineTo(0f, h)
            } else {
                moveTo(0f, 0f)
                lineTo(w, 0f)
                lineTo(w / 2f, h)
            }
            close()
        }
        drawPath(path, color)
    }
}

private fun formatPrice(value: Double): String {
    return String.format(Locale.US, "%.2f", value)
}

private fun formatPercent(value: Double): String {
    return String.format(Locale.US, "%.0f%%", value * 100.0)
}

private fun formatNumber(value: Double): String {
    return String.format(Locale.US, "%.2f", value)
}

private fun formatTimestamp(iso: String): String {
    val zone = ZoneId.systemDefault()
    val formatter = DateTimeFormatter.ofPattern("MMM d, yyyy HH:mm", Locale.US)
    return runCatching {
        val instant = when {
            iso.endsWith("Z", ignoreCase = true) -> Instant.parse(iso)
            else -> OffsetDateTime.parse(iso).toInstant()
        }
        formatter.format(instant.atZone(zone))
    }.getOrElse { iso }
}

@Composable
private fun LuxuryShell(content: @Composable () -> Unit) {
    val colors = MaterialTheme.colorScheme
    val gradient = Brush.verticalGradient(
        0.0f to colors.background,
        0.45f to luxuryPanelColor(),
        1.0f to colors.background
    )
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(gradient)
            .padding(4.dp)
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Color.Transparent
        ) {
            content()
        }
    }
}

private fun luxuryColorScheme(darkMode: Boolean) = if (darkMode) {
    darkColorScheme(
        primary = LuxuryGold,
        secondary = Color(0xFF4DD0E1),
        tertiary = Color(0xFFCA6702),
        background = Color(0xFF0B0F14),
        surface = LuxuryPanel,
        surfaceVariant = LuxuryPanelAlt,
        onPrimary = Color(0xFF1A1400),
        onSecondary = Color(0xFF001316),
        onTertiary = Color(0xFF261A00),
        onBackground = Color(0xFFE8E2D8),
        onSurface = Color(0xFFE8E2D8),
        outline = LuxuryBorder
    )
} else {
    lightColorScheme(
        primary = Color(0xFF8B6B00),
        secondary = Color(0xFF005F73),
        tertiary = Color(0xFF9C6644),
        background = Color(0xFFF7F3EA),
        surface = Color(0xFFFFF9F0),
        surfaceVariant = Color(0xFFF1E7D6),
        onPrimary = Color(0xFFFFF4CF),
        onSecondary = Color(0xFFE6F6FA),
        onTertiary = Color(0xFFFFF2E6),
        onBackground = Color(0xFF1F1A12),
        onSurface = Color(0xFF1F1A12),
        outline = Color(0xFFE0D4C0)
    )
}

private fun luxuryTypography(): Typography {
    return Typography(
        displayLarge = TextStyle(
            fontFamily = FontFamily.Serif,
            fontWeight = FontWeight.SemiBold,
            fontSize = 30.sp
        ),
        titleLarge = TextStyle(
            fontFamily = FontFamily.Serif,
            fontWeight = FontWeight.SemiBold,
            fontSize = 22.sp
        ),
        titleMedium = TextStyle(
            fontFamily = FontFamily.Serif,
            fontWeight = FontWeight.Medium
        ),
        bodyLarge = TextStyle(
            fontFamily = FontFamily.Serif
        ),
        bodyMedium = TextStyle(
            fontFamily = FontFamily.Serif
        ),
        labelSmall = TextStyle(
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Medium
        )
    )
}
