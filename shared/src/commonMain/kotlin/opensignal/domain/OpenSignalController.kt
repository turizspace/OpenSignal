package opensignal.domain

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import opensignal.models.AuthSession
import opensignal.models.CopilotAnalysis
import opensignal.models.ScreenshotPayload
import opensignal.models.Timeframe
import opensignal.settings.SettingsRepository

data class CopilotUiState(
    val isLoading: Boolean = false,
    val authSession: AuthSession? = null,
    val latestAnalysis: CopilotAnalysis? = null,
    val error: String? = null
)

class OpenSignalController(
    private val authGateway: AuthGateway,
    private val analyzeUseCase: AnalyzeScreenshotUseCase,
    private val settingsRepository: SettingsRepository
) {

    private val mutableState = MutableStateFlow(CopilotUiState())
    val state: StateFlow<CopilotUiState> = mutableState

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
            mutableState.update { it.copy(latestAnalysis = response.analysis) }
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
            it.copy(authSession = null, latestAnalysis = null, error = null)
        }
    }

    fun reportError(message: String) {
        mutableState.update { it.copy(error = message) }
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
