package opensignal.domain

import opensignal.models.AuthSession
import opensignal.models.CopilotAnalysis
import opensignal.models.FundamentalAnalysis
import opensignal.models.PublishedSignal
import opensignal.models.ScreenshotPayload
import opensignal.models.TechnicalAnalysis
import opensignal.models.Timeframe
import opensignal.models.TradeSignal
import opensignal.models.UploadedScreenshot

interface AuthGateway {
    suspend fun loginWithNsec(nsec: String, relayHint: String? = null): AuthSession
    suspend fun loginWithExternalSigner(connectionUri: String? = null): AuthSession
    suspend fun loginWithExternalSignerSession(
        pubkey: String,
        packageName: String,
        relayHint: String? = null
    ): AuthSession
}

interface ScreenshotUploader {
    suspend fun upload(payload: ScreenshotPayload): UploadedScreenshot
}

interface ChartVisionAnalyzer {
    suspend fun analyze(
        screenshot: UploadedScreenshot,
        screenshotBytes: ByteArray,
        symbol: String,
        timeframe: Timeframe
    ): TechnicalAnalysis
}

interface FundamentalAnalyzer {
    suspend fun analyze(symbol: String): FundamentalAnalysis
}

interface NostrSignalPublisher {
    suspend fun publish(signal: TradeSignal, relays: List<String>): PublishedSignal
}

data class AnalyzeTradeRequest(
    val symbol: String,
    val timeframe: Timeframe,
    val screenshot: ScreenshotPayload,
    val accountBalance: Double,
    val riskPerTradePercent: Double,
    val maxOpenTrades: Int,
    val minimumRiskReward: Double,
    val leverage: Double,
    val relays: List<String>,
    val userContext: String = ""
)

data class AnalyzeTradeResponse(
    val analysis: CopilotAnalysis,
    val uploadedScreenshot: UploadedScreenshot,
    val technical: TechnicalAnalysis,
    val fundamental: FundamentalAnalysis
)
