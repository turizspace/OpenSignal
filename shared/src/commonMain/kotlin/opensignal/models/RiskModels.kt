package opensignal.models

import kotlinx.serialization.Serializable

@Serializable
data class RiskParameters(
    val accountBalance: Double,
    val riskPerTradePercent: Double,
    val maxOpenTrades: Int,
    val minimumRiskReward: Double,
    val leverage: Double
)

@Serializable
data class TradeOption(
    val side: TradeSide,
    val entry: Double,
    val stopLoss: Double,
    val takeProfit: Double,
    val riskReward: Double,
    val positionSizeUnits: Double,
    val capitalAtRisk: Double,
    val confidence: Double,
    val note: String
)

@Serializable
data class TradePlan(
    val buyOptions: List<TradeOption>,
    val sellOptions: List<TradeOption>
)

@Serializable
data class RiskManagementPlan(
    val parameters: RiskParameters,
    val warnings: List<String>,
    val maxLossPerTrade: Double,
    val maxTotalPortfolioRisk: Double
)
