package opensignal.domain

import kotlinx.datetime.Clock
import opensignal.models.CopilotAnalysis
import opensignal.models.RiskParameters
import opensignal.models.TradeSignal
import opensignal.risk.RiskEngine

class AnalyzeScreenshotUseCase(
    private val screenshotUploader: ScreenshotUploader,
    private val chartVisionAnalyzer: ChartVisionAnalyzer,
    private val fundamentalAnalyzer: FundamentalAnalyzer,
    private val signalPublisher: NostrSignalPublisher,
    private val riskEngine: RiskEngine = RiskEngine()
) {

    suspend operator fun invoke(request: AnalyzeTradeRequest): AnalyzeTradeResponse {
        val uploadedScreenshot = screenshotUploader.upload(request.screenshot)
        val technical = chartVisionAnalyzer.analyze(
            screenshot = uploadedScreenshot,
            screenshotBytes = request.screenshot.bytes,
            symbol = request.symbol,
            timeframe = request.timeframe
        )
        val fundamental = fundamentalAnalyzer.analyze(request.symbol)

        val riskParameters = RiskParameters(
            accountBalance = request.accountBalance,
            riskPerTradePercent = request.riskPerTradePercent,
            maxOpenTrades = request.maxOpenTrades,
            minimumRiskReward = request.minimumRiskReward,
            leverage = request.leverage
        )
        val riskResult = riskEngine.buildPlan(
            technical = technical,
            parameters = riskParameters
        )

        val now = Clock.System.now().toString()
        val signal = TradeSignal(
            id = buildSignalId(request.symbol, technical.timeframe.name),
            symbol = request.symbol,
            timeframe = technical.timeframe,
            screenshot = uploadedScreenshot,
            technical = technical,
            fundamental = fundamental,
            tradePlan = riskResult.plan,
            risk = riskResult.risk,
            confidence = ((technical.trendConfidence + fundamental.score) / 2.0).coerceIn(0.0, 1.0),
            reasoning = buildReasoning(technical.summary, fundamental.summary, request.userContext),
            generatedAtIso = now
        )

        val published = if (request.relays.isNotEmpty()) {
            signalPublisher.publish(signal, request.relays)
        } else {
            null
        }

        return AnalyzeTradeResponse(
            analysis = CopilotAnalysis(
                signal = signal,
                publishedSignal = published
            ),
            uploadedScreenshot = uploadedScreenshot,
            technical = technical,
            fundamental = fundamental
        )
    }

    private fun buildReasoning(
        technicalSummary: String,
        fundamentalSummary: String,
        userContext: String
    ): String {
        return buildString {
            append("Technical: ")
            append(technicalSummary)
            append(" | Fundamental: ")
            append(fundamentalSummary)
            if (userContext.isNotBlank()) {
                append(" | Trader Context: ")
                append(userContext)
            }
        }
    }

    private fun buildSignalId(symbol: String, timeframe: String): String {
        val timestamp = Clock.System.now().toEpochMilliseconds()
        return "sig-${symbol.lowercase()}-${timeframe.lowercase()}-$timestamp"
    }
}
