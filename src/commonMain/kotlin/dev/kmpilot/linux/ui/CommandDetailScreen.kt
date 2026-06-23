package dev.kmpilot.linux.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.kmpilot.linux.domain.Command

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommandDetailScreen(command: Command?, onBack: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(command?.name ?: "Command", fontFamily = FontFamily.Monospace) },
                navigationIcon = { TextButton(onClick = onBack, modifier = Modifier.testTag("back")) { Text("‹ Back") } },
            )
        },
    ) { padding ->
        if (command == null) {
            Text("Not found", Modifier.padding(padding).padding(16.dp))
            return@Scaffold
        }
        Column(
            Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Text(command.category.uppercase(), style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary)
            Text(command.summary, style = MaterialTheme.typography.bodyLarge)
            Section("SYNOPSIS")
            Mono(command.syntax)
            Section("EXAMPLES")
            command.examples.forEach { Mono(it) }
        }
    }
}

@Composable
private fun Section(label: String) =
    Text(label, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurfaceVariant)

@Composable
private fun Mono(text: String) {
    Surface(color = MaterialTheme.colorScheme.surfaceVariant, modifier = Modifier.fillMaxWidth()) {
        Text(text, fontFamily = FontFamily.Monospace, modifier = Modifier.padding(12.dp))
    }
}
