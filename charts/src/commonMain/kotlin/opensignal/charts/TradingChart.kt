package opensignal.charts

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import opensignal.charts.overlays.LiquidityOverlay
import opensignal.charts.overlays.StructureOverlay
import opensignal.charts.overlays.SupportResistanceOverlay
import opensignal.charts.trade_markers.TradeMarkers
import opensignal.models.TechnicalAnalysis
import opensignal.models.TradePlan

@Composable
fun TradingChart(
    technical: TechnicalAnalysis?,
    tradePlan: TradePlan?,
    modifier: Modifier = Modifier,
    showLiquidity: Boolean = true,
    showStructure: Boolean = true
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF0B1422),
                        Color(0xFF121A29)
                    )
                ),
                shape = RoundedCornerShape(16.dp)
            )
            .padding(12.dp)
    ) {
        if (technical == null || tradePlan == null) {
            Text(
                text = "Upload a chart screenshot to generate AI overlays",
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFFB8C6DB)
            )
            return@Box
        }

        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            SupportResistanceOverlay(technical = technical)
            if (showLiquidity) {
                LiquidityOverlay(technical = technical)
            }
            if (showStructure) {
                StructureOverlay(technical = technical)
            }
            TradeMarkers(tradePlan = tradePlan)

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "Trend: ${technical.trend.name}",
                    style = MaterialTheme.typography.labelLarge,
                    color = Color(0xFF80E8FF)
                )
                Text(
                    text = "Price: ${technical.currentPrice}",
                    style = MaterialTheme.typography.labelLarge,
                    color = Color(0xFFC7D2FE)
                )
            }
        }
    }
}
