package opensignal.signals

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import opensignal.models.TradeSignal

object SignalJsonSerializer {
    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    fun toJson(signal: TradeSignal): String = json.encodeToString(signal)

    fun fromJson(value: String): TradeSignal = json.decodeFromString(value)
}
