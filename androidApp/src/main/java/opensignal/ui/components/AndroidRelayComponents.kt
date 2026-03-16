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
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
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
 * Android-optimized relay list editor component.
 * Allows users to add/remove relays and set permissions on mobile devices.
 * Shows NIP-65 fetched relays with connectivity status.
 */
@Composable
fun AndroidRelayListEditor(
    relays: List<RelayConfig>,
    onRelaysChanged: (List<RelayConfig>) -> Unit,
    modifier: Modifier = Modifier,
    readOnly: Boolean = false,
    maxRelays: Int = 5,  // Limit for mobile
    connectedRelays: Set<String> = emptySet(),  // Relays with active connections
    onRefreshRelays: (() -> Unit)? = null  // Callback to fetch NIP-65 relays
) {
    var newRelayUrl by remember { mutableStateOf("") }
    
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Relay Configuration (NIP-65)", style = MaterialTheme.typography.titleSmall)
                    Text(
                        "${relays.size}/$maxRelays relays • ${connectedRelays.size} connected",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (onRefreshRelays != null) {
                    IconButton(onClick = onRefreshRelays) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh relays from NIP-65",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
            
            // List of relays (mobile optimized)
            if (relays.isEmpty()) {
                Text(
                    "No relays configured",
                    style = MaterialTheme.typography.bodySmall
                )
            } else {
                relays.forEach { relay ->
                    AndroidRelayConfigItem(
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
            if (!readOnly && relays.size < maxRelays) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    TextField(
                        value = newRelayUrl,
                        onValueChange = { newRelayUrl = it },
                        label = { Text("Add relay (wss://...)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        textStyle = MaterialTheme.typography.bodySmall
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Button(
                            onClick = {
                                if (newRelayUrl.isNotBlank()) {
                                    onRelaysChanged(relays + RelayConfig(url = newRelayUrl))
                                    newRelayUrl = ""
                                }
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Add")
                        }
                        OutlinedButton(
                            onClick = { newRelayUrl = "" },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Clear")
                        }
                    }
                }
            }
            
            // Quick add presets (mobile optimized)
            if (!readOnly && relays.size < maxRelays) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Quick add:", style = MaterialTheme.typography.labelSmall)
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        OutlinedButton(
                            onClick = { 
                                if (!relays.any { it.url == RelayConstants.PrimaryRelays.DAMUS }) {
                                    onRelaysChanged(relays + RelayConfig(url = RelayConstants.PrimaryRelays.DAMUS))
                                }
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Damus", style = MaterialTheme.typography.labelSmall)
                        }
                        OutlinedButton(
                            onClick = { 
                                if (!relays.any { it.url == RelayConstants.PrimaryRelays.PRIMAL }) {
                                    onRelaysChanged(relays + RelayConfig(url = RelayConstants.PrimaryRelays.PRIMAL))
                                }
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Primal", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }
        }
    }
}

/**
 * Individual relay item for Android with connectivity status.
 */
@Composable
private fun AndroidRelayConfigItem(
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
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // Relay URL, connectivity status, and remove button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        relay.url.replace("wss://", "").take(20),
                        style = MaterialTheme.typography.labelSmall
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
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(4.dp)
                        )
                    }
                }
            }
            
            // Permission toggles - compact for mobile
            if (!readOnly) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
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
                                    if (relay.canWrite()) RelayConstants.RelayPermissions.WRITE else ""
                                }
                                onPermissionChange(newPermissions)
                            },
                            modifier = Modifier.padding(0.dp)
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
                                    if (relay.canRead()) RelayConstants.RelayPermissions.READ else ""
                                }
                                onPermissionChange(newPermissions)
                            },
                            modifier = Modifier.padding(0.dp)
                        )
                        Text("Write", style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        }
    }
}

/**
 * Relay status indicator for Android.
 */
@Composable
fun AndroidRelayStatusIndicator(
    relays: List<String>,
    modifier: Modifier = Modifier
) {
    val hasRelays = relays.isNotEmpty()
    val statusColor = if (hasRelays) Color.Green else Color.Yellow
    
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = MaterialTheme.shapes.small
    ) {
        Row(
            modifier = Modifier
                .padding(8.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (hasRelays) Icons.Default.Check else Icons.Default.CloudOff,
                contentDescription = "Status",
                tint = statusColor,
                modifier = Modifier.padding(4.dp)
            )
            if (hasRelays) {
                Text(
                    "${relays.size} relay${if (relays.size != 1) "s" else ""}",
                    style = MaterialTheme.typography.bodySmall
                )
            } else {
                Text(
                    "No relays",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

/**
 * Share button for Android.
 */
@Composable
fun AndroidShareButton(
    onClick: () -> Unit,
    enabled: Boolean = true,
    modifier: Modifier = Modifier,
    label: String = "Share"
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
        Text(label, style = MaterialTheme.typography.labelMedium)
    }
}
