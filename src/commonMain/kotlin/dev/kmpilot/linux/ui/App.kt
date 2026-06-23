package dev.kmpilot.linux.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import com.arkivanov.essenty.lifecycle.resume
import dev.kmpilot.linux.data.CommandRepository
import dev.kmpilot.linux.presentation.RootComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

/** The shared app UI — identical on Android, iOS, and the wasm preview. Each platform entrypoint calls App(root). */
@Composable
fun App(root: RootComponent) {
    MaterialTheme { RootContent(root) }
}

/** Builds the root component (resumed lifecycle + a Main-dispatcher scope). Used by every platform entrypoint. */
fun buildRoot(scope: CoroutineScope = CoroutineScope(Dispatchers.Main)): RootComponent {
    val lifecycle = LifecycleRegistry()
    val root = RootComponent(DefaultComponentContext(lifecycle), scope, CommandRepository())
    lifecycle.resume()
    return root
}
