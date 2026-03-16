package opensignal.ui.share

import java.util.Locale
import opensignal.models.CopilotAnalysis
import opensignal.models.ShareSection
import opensignal.models.TradeOption

data class ShareContent(
    val title: String,
    val subtitle: String,
    val summary: String,
    val details: String
)

fun buildShareContent(analysis: CopilotAnalysis, section: ShareSection): ShareContent {
    val signal = analysis.signal
    val technical = signal.technical
    val fundamental = signal.fundamental
    val plan = signal.tradePlan
    val subtitle = "${signal.symbol} • ${signal.timeframe.name}"

    return when (section) {
        ShareSection.SIGNAL_OVERVIEW -> ShareContent(
            title = "Signal Overview",
            subtitle = subtitle,
            summary = "${technical.trend.name} • Confidence ${formatPercent(signal.confidence)}",
            details = listOf(
                "Price ${formatPrice(technical.currentPrice)}",
                technical.summary
            ).joinToString(" • ")
        )
        ShareSection.TRADE_PLAN -> ShareContent(
            title = "Trade Plan",
            subtitle = subtitle,
            summary = "Buy ${plan.buyOptions.size} • Sell ${plan.sellOptions.size}",
            details = buildTradePlanDetails(plan.buyOptions, plan.sellOptions)
        )
        ShareSection.TECHNICAL_SUMMARY -> ShareContent(
            title = "Technical Summary",
            subtitle = subtitle,
            summary = technical.summary,
            details = "Structure ${technical.structureEvents.size} • Liquidity ${technical.liquiditySweeps.size} • S/R ${technical.supportResistance.size}"
        )
        ShareSection.FUNDAMENTAL_SUMMARY -> ShareContent(
            title = "Fundamental Summary",
            subtitle = subtitle,
            summary = fundamental.summary,
            details = buildFundamentalDetails(fundamental.overallBias.name, fundamental.score, fundamental.riskFlags)
        )
        ShareSection.NOSTR_PUBLISH -> {
            val published = analysis.publishedSignal
            ShareContent(
                title = "Nostr Publish",
                subtitle = subtitle,
                summary = published?.let { "Event ${it.eventId}" } ?: "Not published yet",
                details = published?.relays?.takeIf { it.isNotEmpty() }?.joinToString(", ")
                    ?: "No relays available"
            )
        }
    }
}

private fun buildTradePlanDetails(
    buyOptions: List<TradeOption>,
    sellOptions: List<TradeOption>
): String {
    val buyPreview = formatOptions("Buy", buyOptions)
    val sellPreview = formatOptions("Sell", sellOptions)
    return listOfNotNull(buyPreview, sellPreview).joinToString("\n")
}

private fun formatOptions(label: String, options: List<TradeOption>): String {
    if (options.isEmpty()) return "$label: None"
    val preview = options.take(2).joinToString("\n") { formatTradeOption(it) }
    val more = if (options.size > 2) " (+${options.size - 2} more)" else ""
    return "$label:\n$preview$more"
}

private fun formatTradeOption(option: TradeOption): String {
    return "${option.side.name} @ ${formatPrice(option.entry)} | SL ${formatPrice(option.stopLoss)} | TP ${formatPrice(option.takeProfit)}"
}

private fun buildFundamentalDetails(bias: String, score: Double, riskFlags: List<String>): String {
    val riskPreview = when {
        riskFlags.isEmpty() -> "No risk flags"
        riskFlags.size <= 2 -> riskFlags.joinToString(", ")
        else -> riskFlags.take(2).joinToString(", ") + " (+${riskFlags.size - 2} more)"
    }
    return "Bias $bias • Score ${formatPercent(score)} • $riskPreview"
}

private fun formatPrice(value: Double): String {
    return String.format(Locale.US, "%.2f", value)
}

private fun formatPercent(value: Double): String {
    return String.format(Locale.US, "%.0f%%", value * 100.0)
}
