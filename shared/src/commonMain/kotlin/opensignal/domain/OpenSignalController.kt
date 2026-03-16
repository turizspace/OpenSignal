package opensignal.domain

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import opensignal.models.AnalysisHistoryEntry
import opensignal.models.AuthSession
import opensignal.models.CopilotAnalysis
import opensignal.models.ScreenshotPayload
import opensignal.models.Timeframe
import opensignal.settings.SettingsRepository

data class CopilotUiState(
    val isLoading: Boolean = false,
    val authSession: AuthSession? = null,
    val latestAnalysis: CopilotAnalysis? = null,
    val analysisHistory: List<AnalysisHistoryEntry> = emptyList(),
    val error: String? = null
)

class OpenSignalController(
    private val authGateway: AuthGateway,
    private val analyzeUseCase: AnalyzeScreenshotUseCase,
    private val settingsRepository: SettingsRepository
) {

    private val mutableState = MutableStateFlow(CopilotUiState())
    val state: StateFlow<CopilotUiState> = mutableState
    private val maxHistoryEntries = 50

    suspend fun loginWithNsec(nsec: String, relayHint: String?) {
        runAndCapture {
            val session = authGateway.loginWithNsec(nsec, relayHint)
            mutableState.update { it.copy(authSession = session) }
        }
    }

    suspend fun loginWithExternalSigner(connectionUri: String?) {
        runAndCapture {
            val session = authGateway.loginWithExternalSigner(connectionUri)
            mutableState.update { it.copy(authSession = session) }
        }
    }

    suspend fun loginWithExternalSignerSession(
        pubkey: String,
        packageName: String,
        relayHint: String?
    ) {
        runAndCapture {
            val session = authGateway.loginWithExternalSignerSession(pubkey, packageName, relayHint)
            mutableState.update { it.copy(authSession = session) }
        }
    }

    suspend fun analyzeScreenshot(
        symbol: String,
        timeframe: Timeframe,
        screenshot: ScreenshotPayload,
        accountBalance: Double,
        riskPercent: Double,
        leverage: Double,
        userContext: String
    ) {
        runAndCapture {
            val settings = settingsRepository.settings.value
            val response = analyzeUseCase(
                AnalyzeTradeRequest(
                    symbol = symbol,
                    timeframe = timeframe,
                    screenshot = screenshot,
                    accountBalance = accountBalance,
                    riskPerTradePercent = riskPercent,
                    maxOpenTrades = 3,
                    minimumRiskReward = 2.0,
                    leverage = leverage,
                    relays = settings.preferredRelays,
                    userContext = userContext
                )
            )
            val entry = AnalysisHistoryEntry(
                id = response.analysis.signal.id,
                analysis = response.analysis,
                createdAtIso = response.analysis.signal.generatedAtIso
            )
            mutableState.update { current ->
                val filteredHistory = current.analysisHistory.filterNot { it.id == entry.id }
                val updatedHistory = listOf(entry) + filteredHistory
                current.copy(
                    latestAnalysis = response.analysis,
                    analysisHistory = updatedHistory.take(maxHistoryEntries)
                )
            }
        }
    }

    suspend fun uploadScreenshotForPrediction(
        symbol: String,
        timeframe: Timeframe,
        screenshot: ScreenshotPayload,
        accountBalance: Double,
        riskPercent: Double,
        leverage: Double,
        userContext: String
    ) {
        analyzeScreenshot(
            symbol = symbol,
            timeframe = timeframe,
            screenshot = screenshot,
            accountBalance = accountBalance,
            riskPercent = riskPercent,
            leverage = leverage,
            userContext = userContext
        )
    }

    fun logout() {
        mutableState.update {
            it.copy(authSession = null, latestAnalysis = null, analysisHistory = emptyList(), error = null)
        }
    }

    fun reportError(message: String) {
        mutableState.update { it.copy(error = message) }
    }

    fun toggleHistoryTraining(entryId: String, enabled: Boolean) {
        mutableState.update { current ->
            current.copy(
                analysisHistory = current.analysisHistory.map { entry ->
                    if (entry.id == entryId) entry.copy(queuedForTraining = enabled) else entry
                }
            )
        }
    }

    fun removeHistoryEntry(entryId: String) {
        mutableState.update { current ->
            current.copy(
                analysisHistory = current.analysisHistory.filterNot { it.id == entryId }
            )
        }
    }

    fun clearHistory() {
        mutableState.update { it.copy(analysisHistory = emptyList()) }
    }

    private suspend fun runAndCapture(block: suspend () -> Unit) {
        mutableState.update { it.copy(isLoading = true, error = null) }
        try {
            block()
        } catch (error: Throwable) {
            mutableState.update { current ->
                current.copy(error = error.message ?: "Unknown error")
            }
        } finally {
            mutableState.update { it.copy(isLoading = false) }
        }
    }
}
