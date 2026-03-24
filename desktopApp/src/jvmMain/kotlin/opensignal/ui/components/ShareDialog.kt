package opensignal.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogState
import androidx.compose.foundation.shape.RoundedCornerShape

/**
 * Share signal dialog component.
 * Allows users to:
 * - Select which relays to share to
 * - Choose share format (full signal or note)
 * - Handle sharing UI with feedback
 */
@Composable
fun ShareSignalDialog(
    shareTitle: String,
    shareSubtitle: String,
    shareSummary: String,
    shareDetails: String,
    availableRelays: List<String>,
    onShare: (selectedRelays: List<String>, format: ShareFormat) -> Unit,
    onDismiss: () -> Unit,
    isSharing: Boolean = false,
    shareError: String? = null,
    defaultFormat: ShareFormat = ShareFormat.TEXT_NOTE
) {
    var selectedRelays by remember { mutableStateOf(availableRelays.toSet()) }
    var selectedFormat by remember { mutableStateOf(defaultFormat) }
    
    // Render as a card-based modal overlay
    Surface(
        modifier = Modifier
            .fillMaxWidth(0.6f)
            .padding(16.dp),
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(12.dp),
        shadowElevation = 8.dp
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Share $shareTitle",
                        style = MaterialTheme.typography.headlineSmall
                    )
                    if (!isSharing) {
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Default.Close, contentDescription = "Close")
                        }
                    }
                }
                
                HorizontalDivider()
                
                // Share summary
                Text(
                    shareSubtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    shareSummary,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (shareDetails.isNotBlank()) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Text(
                            shareDetails,
                            modifier = Modifier.padding(8.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                HorizontalDivider()
                
                // Share format selection
                Text(
                    "Share Format",
                    style = MaterialTheme.typography.titleSmall
                )
                ShareFormatOption(
                    format = ShareFormat.FULL_SIGNAL,
                    title = "Full Signal (kind 30315)",
                    description = "Complete technical analysis with risk management",
                    selected = selectedFormat == ShareFormat.FULL_SIGNAL,
                    onSelect = { selectedFormat = ShareFormat.FULL_SIGNAL }
                )
                ShareFormatOption(
                    format = ShareFormat.TEXT_NOTE,
                    title = "Text Note (kind 1)",
                    description = "Human-readable summary as Nostr note",
                    selected = selectedFormat == ShareFormat.TEXT_NOTE,
                    onSelect = { selectedFormat = ShareFormat.TEXT_NOTE }
                )
                
                HorizontalDivider()
                
                // Relay selection
                Text(
                    "Share to Relays",
                    style = MaterialTheme.typography.titleSmall
                )
                
                if (availableRelays.isEmpty()) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.errorContainer
                    ) {
                        Text(
                            "No relays configured. Configure relays in settings.",
                            modifier = Modifier.padding(8.dp),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                } else {
                    availableRelays.forEach { relay ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = relay in selectedRelays,
                                onCheckedChange = { checked ->
                                    selectedRelays = if (checked) {
                                        selectedRelays + relay
                                    } else {
                                        selectedRelays - relay
                                    }
                                },
                                enabled = !isSharing
                            )
                            Text(
                                relay,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
                
                // Error display
                if (shareError != null) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.errorContainer
                    ) {
                        Text(
                            shareError,
                            modifier = Modifier.padding(8.dp),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
                
                // Action buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        enabled = !isSharing
                    ) {
                        Text("Cancel")
                    }
                    
                    Button(
                        onClick = {
                            if (selectedRelays.isNotEmpty()) {
                                onShare(selectedRelays.toList(), selectedFormat)
                            }
                        },
                        modifier = Modifier.weight(1f),
                        enabled = !isSharing && selectedRelays.isNotEmpty()
                    ) {
                        if (isSharing) {
                            CircularProgressIndicator(
                                modifier = Modifier.padding(end = 4.dp),
                                strokeWidth = 2.dp
                            )
                        }
                        Text(if (isSharing) "Sharing..." else "Share")
                    }
                }
            }
        }
    }
}

/**
 * Share format option selector.
 */
@Composable
private fun ShareFormatOption(
    format: ShareFormat,
    title: String,
    description: String,
    selected: Boolean,
    onSelect: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = if (selected) {
            CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        } else {
            CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = selected,
                onCheckedChange = { onSelect() }
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.bodySmall)
                Text(
                    description,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/**
 * Share completion dialog.
 */
@Composable
fun ShareCompleteDialog(
    eventId: String,
    relayCount: Int,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.7f)
                .padding(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "Signal Shared",
                    style = MaterialTheme.typography.headlineSmall,
                    color = Color.Green
                )
                
                Text(
                    "Published to $relayCount relay${if (relayCount != 1) "s" else ""}",
                    style = MaterialTheme.typography.bodyMedium
                )
                
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = MaterialTheme.shapes.small
                ) {
                    Text(
                        "Event ID: $eventId",
                        modifier = Modifier.padding(8.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Done")
                }
            }
        }
    }
}

/**
 * Share format options.
 */
enum class ShareFormat {
    FULL_SIGNAL,  // Kind 30315: parameterized replaceable event
    TEXT_NOTE     // Kind 1: text note
}
