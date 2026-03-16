package opensignal.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.CloudCircle
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SurfaceDefaults
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
import opensignal.nostr.relay_manager.RelayConfig
import opensignal.nostr.relay_manager.RelayConstants

/**
 * A reusable relay list editor component.
 * Allows users to add/remove relays and set permissions.
 */
@Composable
fun RelayListEditor(
    relays: List<RelayConfig>,
    onRelaysChanged: (List<RelayConfig>) -> Unit,
    modifier: Modifier = Modifier,
    readOnly: Boolean = false,
    connectedRelays: Set<String> = emptySet()  // Relays with active connections
) {
    var newRelayUrl by remember { mutableStateOf("") }
    
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Relay Configuration", style = MaterialTheme.typography.titleMedium)
            
            // List of relays
            if (relays.isEmpty()) {
                Text("No relays configured. Add relays to publish signals.", style = MaterialTheme.typography.bodySmall)
            } else {
                relays.forEach { relay ->
                    RelayConfigItem(
                        relay = relay,
                        isConnected = connectedRelays.contains(relay.url),
                        onRemove = {
                            onRelaysChanged(relays.filterNot { it.url == relay.url })
                        },
                        onPermissionChange = { newPermissions ->
                            onRelaysChanged(relays.map {
                                if (it.url == relay.url) it.copy(permissions = newPermissions) else it
                            })
                        },
                        readOnly = readOnly
                    )
                }
            }
            
            // Add new relay
            if (!readOnly) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    androidx.compose.material3.TextField(
                        value = newRelayUrl,
                        onValueChange = { newRelayUrl = it },
                        label = { Text("Add relay...") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        textStyle = MaterialTheme.typography.bodySmall
                    )
                    Button(
                        onClick = {
                            if (newRelayUrl.isNotBlank()) {
                                onRelaysChanged(relays + RelayConfig(url = newRelayUrl))
                                newRelayUrl = ""
                            }
                        }
                    ) {
                        Text("Add")
                    }
                }
            }
            
            // Quick add presets
            if (!readOnly) {
                QuickRelayPresets(
                    onPresetSelected = { preset ->
                        if (!relays.any { it.url == preset.url }) {
                            onRelaysChanged(relays + preset)
                        }
                    }
                )
            }
        }
    }
}

/**
 * Individual relay configuration item with permission controls and connectivity status.
 */
@Composable
private fun RelayConfigItem(
    relay: RelayConfig,
    isConnected: Boolean = false,
    onRemove: () -> Unit,
    onPermissionChange: (String) -> Unit,
    readOnly: Boolean = false
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        relay.url,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                // Connectivity status icon
                Icon(
                    imageVector = if (isConnected) Icons.Default.CloudCircle else Icons.Default.CloudOff,
                    contentDescription = if (isConnected) "Connected" else "Disconnected",
                    tint = if (isConnected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(4.dp)
                )
                if (!readOnly) {
                    IconButton(onClick = onRemove, modifier = Modifier.padding(0.dp)) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete relay",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
            
            // Permission toggles
            if (!readOnly) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Checkbox(
                            checked = relay.canRead(),
                            onCheckedChange = { checked ->
                                val newPermissions = if (checked) {
                                    if (relay.canWrite()) RelayConstants.RelayPermissions.READ_WRITE
                                    else RelayConstants.RelayPermissions.READ
                                } else {
                                    if (relay.canWrite()) RelayConstants.RelayPermissions.WRITE
                                    else ""
                                }
                                onPermissionChange(newPermissions)
                            }
                        )
                        Text("Read", style = MaterialTheme.typography.labelSmall)
                    }
                    
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Checkbox(
                            checked = relay.canWrite(),
                            onCheckedChange = { checked ->
                                val newPermissions = if (checked) {
                                    if (relay.canRead()) RelayConstants.RelayPermissions.READ_WRITE
                                    else RelayConstants.RelayPermissions.WRITE
                                } else {
                                    if (relay.canRead()) RelayConstants.RelayPermissions.READ
                                    else ""
                                }
                                onPermissionChange(newPermissions)
                            }
                        )
                        Text("Write", style = MaterialTheme.typography.labelSmall)
                    }
                }
            } else {
                Text(
                    "Permissions: ${relay.permissions}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/**
 * Quick relay preset buttons.
 */
@Composable
private fun QuickRelayPresets(onPresetSelected: (RelayConfig) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text("Quick add presets:", style = MaterialTheme.typography.labelSmall)
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            OutlinedButton(
                onClick = { onPresetSelected(RelayConfig(url = RelayConstants.PrimaryRelays.DAMUS)) },
                modifier = Modifier.weight(1f)
            ) {
                Text("Damus", style = MaterialTheme.typography.labelSmall)
            }
            OutlinedButton(
                onClick = { onPresetSelected(RelayConfig(url = RelayConstants.PrimaryRelays.PRIMAL)) },
                modifier = Modifier.weight(1f)
            ) {
                Text("Primal", style = MaterialTheme.typography.labelSmall)
            }
            OutlinedButton(
                onClick = { onPresetSelected(RelayConfig(url = RelayConstants.PrimaryRelays.NOSTR_BAND)) },
                modifier = Modifier.weight(1f)
            ) {
                Text("Nostr Band", style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}

/**
 * Relay status indicator showing connection state.
 */
@Composable
fun RelayStatusIndicator(
    relays: List<String>,
    readableCount: Int = 0,
    writeableCount: Int = 0,
    modifier: Modifier = Modifier
) {
    val hasRelays = relays.isNotEmpty()
    val statusColor = if (hasRelays) Color.Green else Color.Yellow
    val statusText = if (hasRelays) {
        "Connected: ${relays.size} relays"
    } else {
        "No relays configured"
    }
    
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = MaterialTheme.shapes.small
    ) {
        Row(
            modifier = Modifier
                .padding(8.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = if (hasRelays) Icons.Default.Check else Icons.Default.Error,
                    contentDescription = "Status",
                    tint = statusColor,
                    modifier = Modifier.padding(4.dp)
                )
                Column {
                    Text(statusText, style = MaterialTheme.typography.bodySmall)
                    if (hasRelays && (readableCount > 0 || writeableCount > 0)) {
                        Text(
                            "$readableCount read, $writeableCount write",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

/**
 * Share button with menu for different share options.
 */
@Composable
fun ShareButton(
    onClick: () -> Unit,
    enabled: Boolean = true,
    modifier: Modifier = Modifier,
    label: String = "Share Signal"
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
    ) {
        Icon(
            imageVector = Icons.Default.Share,
            contentDescription = "Share",
            modifier = Modifier.padding(end = 4.dp)
        )
        Text(label)
    }
}
