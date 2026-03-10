package opensignal.models

import kotlinx.serialization.Serializable

@Serializable
data class FundamentalFactor(
    val title: String,
    val impact: String,
    val bias: TrendDirection,
    val confidence: Double,
    val details: String
)

@Serializable
data class FundamentalAnalysis(
    val summary: String,
    val overallBias: TrendDirection,
    val score: Double,
    val factors: List<FundamentalFactor>,
    val riskFlags: List<String>
)
