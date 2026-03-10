package opensignal.models

import kotlinx.serialization.Serializable

@Serializable
enum class TrendDirection {
    BULLISH,
    BEARISH,
    SIDEWAYS
}

@Serializable
enum class TradeSide {
    BUY,
    SELL
}

@Serializable
enum class Timeframe {
    M1,
    M5,
    M15,
    M30,
    H1,
    H4,
    D1,
    W1
}

@Serializable
enum class LevelKind {
    SUPPORT,
    RESISTANCE
}

@Serializable
enum class PatternType {
    LIQUIDITY_SWEEP,
    BREAK_OF_STRUCTURE,
    CHANGE_OF_CHARACTER,
    FAIR_VALUE_GAP,
    RANGE
}
