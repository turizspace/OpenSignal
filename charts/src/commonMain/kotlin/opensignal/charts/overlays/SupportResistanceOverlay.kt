package opensignal.charts.overlays

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import opensignal.models.LevelKind
import opensignal.models.TechnicalAnalysis

@Composable
fun SupportResistanceOverlay(technical: TechnicalAnalysis) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Support / Resistance",
            color = Color(0xFF9CC0FF)
        )
        technical.supportResistance.forEach { level ->
            val color = if (level.kind == LevelKind.SUPPORT) Color(0xFF6EE7B7) else Color(0xFFFDA4AF)
            Row(modifier = Modifier.padding(vertical = 2.dp)) {
                Text(
                    text = "${level.label}: ${level.price}",
                    color = color,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
