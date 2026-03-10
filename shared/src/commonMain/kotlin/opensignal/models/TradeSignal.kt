package opensignal.models

import kotlinx.serialization.Serializable

@Serializable
data class TradeSignal(
    val id: String,
    val symbol: String,
    val timeframe: Timeframe,
    val screenshot: UploadedScreenshot,
    val technical: TechnicalAnalysis,
    val fundamental: FundamentalAnalysis,
    val tradePlan: TradePlan,
    val risk: RiskManagementPlan,
    val confidence: Double,
    val reasoning: String,
    val generatedAtIso: String
)

@Serializable
data class PublishedSignal(
    val eventId: String,
    val relays: List<String>,
    val publishedAtIso: String
)

@Serializable
data class CopilotAnalysis(
    val signal: TradeSignal,
    val publishedSignal: PublishedSignal?
)
