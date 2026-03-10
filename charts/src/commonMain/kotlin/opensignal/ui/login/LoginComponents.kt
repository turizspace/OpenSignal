package opensignal.ui.login

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun NsecLoginCard(
    onLogin: (String) -> Unit,
    title: String = "Login with nsec",
    helperText: String = "Use your private nsec key to authenticate.",
    initialValue: String = ""
) {
    var nsec by remember { mutableStateOf(initialValue) }

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Text(helperText, style = MaterialTheme.typography.bodySmall)
            OutlinedTextField(
                value = nsec,
                onValueChange = { nsec = it },
                label = { Text("Nostr nsec") },
                modifier = Modifier.fillMaxWidth()
            )
            Button(onClick = { onLogin(nsec) }) {
                Text("Login with nsec")
            }
        }
    }
}

@Composable
fun ExternalSignerLoginCard(
    onLogin: (String) -> Unit,
    title: String = "Login with external signer",
    helperText: String = "Use a Nostr signer or bunker connection URI.",
    initialValue: String = "bunker://nostr-signer"
) {
    var signerUri by remember { mutableStateOf(initialValue) }

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Text(helperText, style = MaterialTheme.typography.bodySmall)
            OutlinedTextField(
                value = signerUri,
                onValueChange = { signerUri = it },
                label = { Text("External signer URI") },
                modifier = Modifier.fillMaxWidth()
            )
            Button(onClick = { onLogin(signerUri) }) {
                Text("Login with external signer")
            }
        }
    }
}
