package dev.kmpilot.linux.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.arkivanov.decompose.extensions.compose.stack.Children
import dev.kmpilot.linux.presentation.RootComponent

@Composable
fun RootContent(root: RootComponent) {
    Children(stack = root.stack) { child ->
        when (val i = child.instance) {
            is RootComponent.Child.Commands -> {
                val ui by i.component.ui.collectAsState()
                val query by i.component.query.collectAsState()
                CommandsScreen(
                    ui = ui, query = query, onQuery = i.component::setQuery, onOpen = i.component.onOpen,
                    onBasics = i.component.onBasics, onSettings = i.component.onSettings, onRetry = i.component::retry,
                )
            }
            is RootComponent.Child.CommandDetail -> CommandDetailScreen(i.component.command, i.component.onBack)
            is RootComponent.Child.Basics -> BasicsScreen(i.component.groups, i.component.onOpen, i.component.onBack)
            is RootComponent.Child.Settings -> SettingsScreen(i.component.onBack)
        }
    }
}
