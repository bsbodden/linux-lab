package dev.kmpilot.linux.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.kmpilot.linux.domain.Command
import dev.kmpilot.linux.presentation.CommandsUi

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommandsScreen(
    ui: CommandsUi,
    query: String,
    onQuery: (String) -> Unit,
    onOpen: (String) -> Unit,
    onBasics: () -> Unit,
    onSettings: () -> Unit,
    onRetry: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Linux Commands") },
                actions = {
                    TextButton(onClick = onBasics, modifier = Modifier.testTag("basics")) { Text("Basics") }
                    TextButton(onClick = onSettings, modifier = Modifier.testTag("settings")) { Text("Settings") }
                },
            )
        },
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            OutlinedTextField(
                value = query,
                onValueChange = onQuery,
                placeholder = { Text("Search commands…") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth().padding(16.dp).testTag("search"),
            )
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                when (ui) {
                    is CommandsUi.Loading -> CircularProgressIndicator(Modifier.testTag("loading"))
                    is CommandsUi.Empty -> Text("No commands match “$query”", Modifier.testTag("empty"))
                    is CommandsUi.Error -> Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Text(ui.message, Modifier.testTag("error"))
                        Button(onClick = onRetry, modifier = Modifier.testTag("retry")) { Text("Retry") }
                    }
                    is CommandsUi.Content -> LazyColumn(Modifier.fillMaxSize()) {
                        items(ui.commands, key = { it.name }) { cmd ->
                            CommandRow(cmd, onClick = { onOpen(cmd.name) })
                            HorizontalDivider()
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CommandRow(cmd: Command, onClick: () -> Unit) {
    Column(
        Modifier.fillMaxWidth().clickable(onClick = onClick).padding(horizontal = 16.dp, vertical = 12.dp),
    ) {
        Text(cmd.name, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold,
            modifier = Modifier.testTag("cmd_${cmd.name}"))
        Text(cmd.summary, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
