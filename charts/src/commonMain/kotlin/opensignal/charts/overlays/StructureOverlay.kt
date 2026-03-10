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

@Composable
fun StructureOverlay(technical: TechnicalAnalysis) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Market Structure",
            color = Color(0xFFF59E0B)
        )
        technical.structureEvents.forEach { event ->
            Text(
                text = "${event.type.name} | ${event.direction.name} | ${event.confidence}",
                modifier = Modifier.padding(vertical = 2.dp),
                color = Color(0xFFFDE68A),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
