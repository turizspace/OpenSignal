package opensignal.models

import kotlinx.serialization.Serializable

@Serializable
data class PriceLevel(
    val label: String,
    val price: Double,
    val strength: Double,
    val kind: LevelKind
)

@Serializable
data class LiquiditySweep(
    val side: TradeSide,
    val level: Double,
    val confidence: Double,
    val note: String
)

@Serializable
data class StructureEvent(
    val type: PatternType,
    val direction: TrendDirection,
    val confidence: Double,
    val description: String
)

@Serializable
data class TechnicalAnalysis(
    val symbol: String,
    val timeframe: Timeframe,
    val currentPrice: Double,
    val trend: TrendDirection,
    val trendConfidence: Double,
    val supportResistance: List<PriceLevel>,
    val liquiditySweeps: List<LiquiditySweep>,
    val structureEvents: List<StructureEvent>,
    val summary: String
)
