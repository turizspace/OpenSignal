package opensignal.ai.vision

import opensignal.models.Timeframe

data class AxisTick(val price: Double, val y: Int)

data class PriceScale(val slope: Double, val intercept: Double) {
    fun priceAt(y: Int): Double = slope * y + intercept
}

object OcrParsing {
    fun extractTimeframe(words: List<OcrWord>, timeframeRect: IntRect): Timeframe? {
        val candidates = words.filter { it.box.intersects(timeframeRect) }
        val tokens = candidates.flatMap { word -> word.text.split(' ', '/', '-', '|') }
        tokens.forEach { token ->
            parseTimeframeToken(token)?.let { return it }
        }
        return null
    }

    fun extractAxisTicks(words: List<OcrWord>, axisRect: IntRect): List<AxisTick> {
        return words.filter { it.box.intersects(axisRect) }
            .mapNotNull { word ->
                val price = parsePrice(word.text) ?: return@mapNotNull null
                AxisTick(price = price, y = word.box.centerY)
            }
    }

    fun detectCurrentPrice(words: List<OcrWord>, chartRect: IntRect, axisRect: IntRect): Double? {
        val rightZone = IntRect(
            left = (chartRect.right - chartRect.width * 0.25f).toInt(),
            top = chartRect.top,
            right = chartRect.right,
            bottom = chartRect.bottom
        )
        val candidates = words.filter { it.box.intersects(axisRect) || it.box.intersects(rightZone) }
            .mapNotNull { word ->
                val price = parsePrice(word.text) ?: return@mapNotNull null
                val height = word.box.height.toFloat().coerceAtLeast(1f)
                price to height
            }

        return candidates.maxByOrNull { it.second }?.first
    }

    fun calibrateAxis(ticks: List<AxisTick>): PriceScale? {
        if (ticks.size < 2) return null
        val sorted = ticks.sortedBy { it.y }
        val low = sorted.first()
        val high = sorted.last()
        val dy = (high.y - low.y).toDouble()
        if (dy == 0.0) return null
        val slope = (high.price - low.price) / dy
        val intercept = low.price - slope * low.y
        return PriceScale(slope = slope, intercept = intercept)
    }

    private fun parseTimeframeToken(raw: String): Timeframe? {
        val token = raw.trim().lowercase()
        return when (token) {
            "1m", "m1" -> Timeframe.M1
            "5m", "m5" -> Timeframe.M5
            "15m", "m15" -> Timeframe.M15
            "30m", "m30" -> Timeframe.M30
            "1h", "h1" -> Timeframe.H1
            "4h", "h4" -> Timeframe.H4
            "1d", "d1" -> Timeframe.D1
            "1w", "w1" -> Timeframe.W1
            else -> null
        }
    }

    private fun parsePrice(raw: String): Double? {
        val cleaned = raw.replace("$", "").replace(",", "").trim()
        val regex = """\d+(?:\.\d+)?""".toRegex()
        val match = regex.find(cleaned) ?: return null
        return match.value.toDoubleOrNull()
    }
}
