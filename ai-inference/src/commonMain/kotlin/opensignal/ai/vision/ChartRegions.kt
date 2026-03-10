package opensignal.ai.vision

data class IntRect(
    val left: Int,
    val top: Int,
    val right: Int,
    val bottom: Int
) {
    val width: Int = (right - left).coerceAtLeast(0)
    val height: Int = (bottom - top).coerceAtLeast(0)
    val centerX: Int = left + width / 2
    val centerY: Int = top + height / 2

    fun contains(x: Int, y: Int): Boolean {
        return x in left..right && y in top..bottom
    }

    fun intersects(other: IntRect): Boolean {
        return left < other.right && right > other.left && top < other.bottom && bottom > other.top
    }
}

data class ChartRegion(
    val chartRect: IntRect,
    val priceAxisRect: IntRect,
    val timeframeRect: IntRect
)

object ChartRegionDetector {
    fun detect(imageWidth: Int, imageHeight: Int): ChartRegion {
        val topPad = (imageHeight * 0.07f).toInt()
        val bottomPad = (imageHeight * 0.05f).toInt()
        val leftPad = (imageWidth * 0.02f).toInt()
        val rightAxisWidth = (imageWidth * 0.18f).toInt()

        val chartRect = IntRect(
            left = leftPad,
            top = topPad,
            right = (imageWidth - rightAxisWidth).coerceAtLeast(leftPad + 1),
            bottom = (imageHeight - bottomPad).coerceAtLeast(topPad + 1)
        )

        val priceAxisRect = IntRect(
            left = chartRect.right,
            top = chartRect.top,
            right = imageWidth,
            bottom = chartRect.bottom
        )

        val timeframeRect = IntRect(
            left = leftPad,
            top = 0,
            right = (imageWidth * 0.35f).toInt().coerceAtLeast(leftPad + 1),
            bottom = topPad.coerceAtLeast(1)
        )

        return ChartRegion(
            chartRect = chartRect,
            priceAxisRect = priceAxisRect,
            timeframeRect = timeframeRect
        )
    }
}
