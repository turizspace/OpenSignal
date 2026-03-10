package opensignal.charts.trade_markers

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import opensignal.models.TradePlan
import opensignal.models.TradeSide

@Composable
fun TradeMarkers(tradePlan: TradePlan) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Trade Entries",
            color = Color(0xFF93C5FD)
        )

        (tradePlan.buyOptions + tradePlan.sellOptions).forEach { option ->
            val color = if (option.side == TradeSide.BUY) Color(0xFF86EFAC) else Color(0xFFFCA5A5)
            Text(
                text = "${option.side.name} entry ${option.entry}, SL ${option.stopLoss}, TP ${option.takeProfit}, RR ${option.riskReward}",
                color = color,
                modifier = Modifier.padding(vertical = 2.dp),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
