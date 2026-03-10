package opensignal.domain

import opensignal.models.Timeframe

fun parseTimeframeOrDefault(value: String): Timeframe {
    return Timeframe.entries.firstOrNull { it.name.equals(value.trim(), ignoreCase = true) }
        ?: Timeframe.H1
}

fun pseudoImageBytes(seed: String): ByteArray {
    val base = if (seed.isBlank()) "opensignal" else seed
    return buildString {
        repeat(128) { append(base) }
    }.encodeToByteArray()
}
