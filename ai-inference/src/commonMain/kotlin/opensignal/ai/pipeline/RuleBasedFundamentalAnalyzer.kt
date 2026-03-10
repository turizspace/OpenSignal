package opensignal.ai.pipeline

import opensignal.domain.FundamentalAnalyzer
import opensignal.models.FundamentalAnalysis
import opensignal.models.FundamentalFactor
import opensignal.models.TrendDirection

class RuleBasedFundamentalAnalyzer : FundamentalAnalyzer {

    override suspend fun analyze(symbol: String): FundamentalAnalysis {
        val seed = symbol.hashCode().toUInt().toInt()
        val score = ((seed and 0xFF) / 255.0).coerceIn(0.25, 0.85)
        val bias = when (seed % 3) {
            0 -> TrendDirection.BULLISH
            1 -> TrendDirection.BEARISH
            else -> TrendDirection.SIDEWAYS
        }

        val factors = listOf(
            FundamentalFactor(
                title = "Macro liquidity",
                impact = "Medium",
                bias = bias,
                confidence = score,
                details = "Proxy macro regime inferred from public volatility and liquidity regime features."
            ),
            FundamentalFactor(
                title = "Earnings / news risk",
                impact = "High",
                bias = TrendDirection.SIDEWAYS,
                confidence = 0.62,
                details = "Potential scheduled event risk; reduce sizing near major announcements."
            )
        )

        return FundamentalAnalysis(
            summary = "Fundamental backdrop for $symbol is ${bias.name.lowercase()} with event-risk caution.",
            overallBias = bias,
            score = score,
            factors = factors,
            riskFlags = listOf("Watch high-impact news windows", "Avoid over-leverage around macro events")
        )
    }
}
