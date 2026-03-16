package opensignal.ui.history

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import opensignal.DesktopAnalysisPanel
import opensignal.models.AnalysisHistoryEntry
import opensignal.models.ShareSection
import opensignal.settings.UserSettings
import opensignal.ui.components.ShareSignalDialog
import opensignal.ui.components.ShareFormat
import opensignal.ui.share.ShareContent
import opensignal.ui.share.buildShareContent

@Composable
fun DesktopHistoryScreen(
    history: List<AnalysisHistoryEntry>,
    settings: UserSettings,
    isLoggedIn: Boolean,
    onToggleTraining: (String, Boolean) -> Unit,
    onClearHistory: () -> Unit
) {
    var selectedEntryId by remember { mutableStateOf<String?>(null) }
    var trainingMessage by remember { mutableStateOf<String?>(null) }
    var showShareDialog by remember { mutableStateOf(false) }
    var shareTarget by remember { mutableStateOf<AnalysisHistoryEntry?>(null) }
    var isSharing by remember { mutableStateOf(false) }
    var shareError by remember { mutableStateOf<String?>(null) }
    var shareSection by remember { mutableStateOf(ShareSection.SIGNAL_OVERVIEW) }
    var shareContent by remember { mutableStateOf<ShareContent?>(null) }
    val scrollState = rememberScrollState()

    LaunchedEffect(history) {
        if (history.isEmpty()) {
            selectedEntryId = null
        } else if (selectedEntryId == null || history.none { it.id == selectedEntryId }) {
            selectedEntryId = history.first().id
        }
    }

    val queuedCount = history.count { it.queuedForTraining }
    val selectedEntry = history.firstOrNull { it.id == selectedEntryId }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        HistorySummaryCard(
            total = history.size,
            queued = queuedCount,
            latestIso = history.firstOrNull()?.createdAtIso,
            onClear = onClearHistory,
            onPrepareTraining = {
                trainingMessage = if (queuedCount == 0) {
                    "Queue at least one analysis to prepare a training bundle."
                } else {
                    "Prepared $queuedCount queued analyses for ONNX training. Export hook pending."
                }
            }
        )

        trainingMessage?.let {
            Text(
                it,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        if (history.isEmpty()) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surfaceVariant
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("No analysis history yet.", style = MaterialTheme.typography.titleSmall)
                    Text(
                        "Run an analysis to start building a training-ready history.",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        } else {
            history.forEach { entry ->
                HistoryEntryCard(
                    entry = entry,
                    isSelected = entry.id == selectedEntryId,
                    onSelect = { selectedEntryId = entry.id },
                    onToggleTraining = { enabled ->
                        onToggleTraining(entry.id, enabled)
                    }
                )
            }
        }

        selectedEntry?.let { entry ->
            Spacer(modifier = Modifier.height(4.dp))
            Text("Selected Analysis", style = MaterialTheme.typography.titleMedium)
            DesktopAnalysisPanel(
                analysis = entry.analysis,
                settings = settings,
                isLoggedIn = isLoggedIn,
                onShowShare = { section ->
                    shareSection = section
                    shareTarget = entry
                    shareContent = buildShareContent(entry.analysis, section)
                    shareError = null
                    showShareDialog = true
                }
            )
        }
    }

    if (showShareDialog && shareTarget != null) {
        val target = shareTarget!!
        val content = shareContent ?: buildShareContent(target.analysis, shareSection)
        ShareSignalDialog(
            shareTitle = content.title,
            shareSubtitle = content.subtitle,
            shareSummary = content.summary,
            shareDetails = content.details,
            availableRelays = settings.preferredRelays,
            onShare = { selectedRelays, format ->
                isSharing = true
                shareError = null
                try {
                    showShareDialog = false
                } catch (e: Exception) {
                    shareError = e.message ?: "Failed to share signal"
                } finally {
                    isSharing = false
                }
            },
            onDismiss = { showShareDialog = false },
            isSharing = isSharing,
            shareError = shareError,
            defaultFormat = if (shareSection == ShareSection.SIGNAL_OVERVIEW) {
                ShareFormat.FULL_SIGNAL
            } else {
                ShareFormat.TEXT_NOTE
            }
        )
    }
}

@Composable
private fun HistorySummaryCard(
    total: Int,
    queued: Int,
    latestIso: String?,
    onClear: () -> Unit,
    onPrepareTraining: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("Analysis History", style = MaterialTheme.typography.titleMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                HistoryChip(label = "Total", value = total.toString())
                HistoryChip(label = "Queued", value = queued.toString())
                latestIso?.let { HistoryChip(label = "Latest", value = formatHistoryTimestamp(it)) }
            }
            Text(
                "Queue entries to assemble a dataset for ONNX fine-tuning and future co-pilot improvements.",
                style = MaterialTheme.typography.bodySmall
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = onClear, enabled = total > 0) {
                    Text("Clear History")
                }
                Button(onClick = onPrepareTraining, enabled = queued > 0) {
                    Text("Prepare Training")
                }
            }
        }
    }
}

@Composable
private fun HistoryChip(label: String, value: String) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        shape = MaterialTheme.shapes.small,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)) {
            Text(label, style = MaterialTheme.typography.labelSmall)
            Text(value, style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
private fun HistoryEntryCard(
    entry: AnalysisHistoryEntry,
    isSelected: Boolean,
    onSelect: () -> Unit,
    onToggleTraining: (Boolean) -> Unit
) {
    val signal = entry.analysis.signal
    Card(
        modifier = Modifier.fillMaxWidth(),
        border = if (isSelected) BorderStroke(1.dp, MaterialTheme.colorScheme.primary) else null,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "${signal.symbol} • ${signal.timeframe.name}",
                        style = MaterialTheme.typography.titleSmall
                    )
                    Text(
                        formatHistoryTimestamp(entry.createdAtIso),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                OutlinedButton(onClick = onSelect) {
                    Text(if (isSelected) "Selected" else "Open")
                }
            }

            Text(
                signal.technical.summary,
                style = MaterialTheme.typography.bodySmall,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                "Trend: ${signal.technical.trend.name} • Confidence: ${formatPercent(signal.confidence)}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = entry.queuedForTraining,
                    onCheckedChange = onToggleTraining
                )
                Text("Queue for training", style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

private fun formatHistoryTimestamp(iso: String): String {
    val zone = ZoneId.systemDefault()
    val formatter = DateTimeFormatter.ofPattern("MMM d, yyyy HH:mm", Locale.US)
    return runCatching {
        val instant = when {
            iso.endsWith("Z", ignoreCase = true) -> Instant.parse(iso)
            else -> OffsetDateTime.parse(iso).toInstant()
        }
        formatter.format(instant.atZone(zone))
    }.getOrElse { iso }
}

private fun formatPercent(value: Double): String {
    return String.format(Locale.US, "%.0f%%", value * 100.0)
}
