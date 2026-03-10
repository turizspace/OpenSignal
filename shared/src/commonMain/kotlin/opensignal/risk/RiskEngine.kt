package opensignal.risk

import kotlin.math.abs
import kotlin.math.max
import opensignal.models.LevelKind
import opensignal.models.RiskManagementPlan
import opensignal.models.RiskParameters
import opensignal.models.TechnicalAnalysis
import opensignal.models.TradeOption
import opensignal.models.TradePlan
import opensignal.models.TradeSide

data class RiskEngineResult(
    val plan: TradePlan,
    val risk: RiskManagementPlan
)

class RiskEngine {

    fun buildPlan(technical: TechnicalAnalysis, parameters: RiskParameters): RiskEngineResult {
        val supports = technical.supportResistance
            .filter { it.kind == LevelKind.SUPPORT }
            .sortedByDescending { it.strength }
        val resistances = technical.supportResistance
            .filter { it.kind == LevelKind.RESISTANCE }
            .sortedByDescending { it.strength }

        val current = technical.currentPrice
        val baseRisk = parameters.accountBalance * (parameters.riskPerTradePercent / 100.0)

        val strongestSupport = supports.firstOrNull()?.price ?: current * 0.995
        val strongestResistance = resistances.firstOrNull()?.price ?: current * 1.005

        val buyPrimary = buildOption(
            side = TradeSide.BUY,
            entry = current,
            stop = minOf(strongestSupport, current * 0.995),
            target = max(
                strongestResistance,
                current + abs(current - strongestSupport) * parameters.minimumRiskReward
            ),
            confidence = technical.trendConfidence,
            baseRisk = baseRisk,
            leverage = parameters.leverage,
            note = "Trend-following buy from current price"
        )

        val buySecondary = buildOption(
            side = TradeSide.BUY,
            entry = strongestSupport,
            stop = strongestSupport * 0.997,
            target = max(
                strongestResistance,
                strongestSupport + abs(strongestSupport - strongestSupport * 0.997) *
                    (parameters.minimumRiskReward + 0.5)
            ),
            confidence = (technical.trendConfidence * 0.92).coerceAtLeast(0.35),
            baseRisk = baseRisk,
            leverage = parameters.leverage,
            note = "Pullback buy from strongest support"
        )

        val sellPrimary = buildOption(
            side = TradeSide.SELL,
            entry = current,
            stop = max(strongestResistance, current * 1.005),
            target = minOf(
                strongestSupport,
                current - abs(strongestResistance - current) * parameters.minimumRiskReward
            ),
            confidence = technical.trendConfidence,
            baseRisk = baseRisk,
            leverage = parameters.leverage,
            note = "Trend-following sell from current price"
        )

        val sellSecondary = buildOption(
            side = TradeSide.SELL,
            entry = strongestResistance,
            stop = strongestResistance * 1.003,
            target = minOf(
                strongestSupport,
                strongestResistance - abs(strongestResistance * 1.003 - strongestResistance) *
                    (parameters.minimumRiskReward + 0.5)
            ),
            confidence = (technical.trendConfidence * 0.92).coerceAtLeast(0.35),
            baseRisk = baseRisk,
            leverage = parameters.leverage,
            note = "Rejection sell from strongest resistance"
        )

        val options = listOf(buyPrimary, buySecondary, sellPrimary, sellSecondary)
        val warnings = mutableListOf<String>()
        options.filter { it.riskReward < parameters.minimumRiskReward }.forEach {
            warnings += "${it.side} option at ${it.entry} is below minimum R:R"
        }

        if (parameters.riskPerTradePercent > 2.0) {
            warnings += "Risk per trade above 2% is aggressive for volatile assets"
        }

        val riskPlan = RiskManagementPlan(
            parameters = parameters,
            warnings = warnings,
            maxLossPerTrade = baseRisk,
            maxTotalPortfolioRisk = baseRisk * parameters.maxOpenTrades
        )

        return RiskEngineResult(
            plan = TradePlan(
                buyOptions = listOf(buyPrimary, buySecondary),
                sellOptions = listOf(sellPrimary, sellSecondary)
            ),
            risk = riskPlan
        )
    }

    private fun buildOption(
        side: TradeSide,
        entry: Double,
        stop: Double,
        target: Double,
        confidence: Double,
        baseRisk: Double,
        leverage: Double,
        note: String
    ): TradeOption {
        val stopDistance = abs(entry - stop).coerceAtLeast(entry * 0.0005)
        val rewardDistance = abs(target - entry).coerceAtLeast(entry * 0.0005)
        val rr = rewardDistance / stopDistance
        val positionUnits = (baseRisk / stopDistance) * leverage

        return TradeOption(
            side = side,
            entry = entry,
            stopLoss = stop,
            takeProfit = target,
            riskReward = rr,
            positionSizeUnits = positionUnits,
            capitalAtRisk = baseRisk,
            confidence = confidence,
            note = note
        )
    }
}
