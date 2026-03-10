package opensignal.charts.overlays

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import opensignal.models.TechnicalAnalysis
import opensignal.models.TradeSide

@Composable
fun LiquidityOverlay(technical: TechnicalAnalysis) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Liquidity Sweeps",
            color = Color(0xFFFDE68A)
        )
        technical.liquiditySweeps.forEach { sweep ->
            val color = if (sweep.side == TradeSide.BUY) Color(0xFF34D399) else Color(0xFFF87171)
            Text(
                text = "${sweep.side.name} @ ${sweep.level} (${sweep.confidence}) - ${sweep.note}",
                modifier = Modifier.padding(vertical = 2.dp),
                color = color,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
