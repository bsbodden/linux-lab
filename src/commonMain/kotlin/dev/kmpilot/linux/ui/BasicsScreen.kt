package dev.kmpilot.linux.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
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
fun BasicsScreen(groups: List<Pair<String, List<Command>>>, onOpen: (String) -> Unit, onBack: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Basics") },
                navigationIcon = { TextButton(onClick = onBack, modifier = Modifier.testTag("back")) { Text("‹ Back") } },
            )
        },
    ) { padding ->
        LazyColumn(Modifier.fillMaxSize().padding(padding)) {
            groups.forEach { (category, commands) ->
                item(key = "h_$category") {
                    Text(category.uppercase(), style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.fillMaxWidth().padding(start = 16.dp, top = 18.dp, bottom = 6.dp))
                }
                items(commands, key = { it.name }) { cmd ->
                    Text(cmd.name, fontFamily = FontFamily.Monospace,
                        modifier = Modifier.fillMaxWidth().clickable { onOpen(cmd.name) }
                            .padding(horizontal = 16.dp, vertical = 12.dp).testTag("basic_${cmd.name}"))
                    HorizontalDivider()
                }
            }
        }
    }
}
