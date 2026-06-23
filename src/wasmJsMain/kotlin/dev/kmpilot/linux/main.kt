package dev.kmpilot.linux

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import dev.kmpilot.linux.presentation.RootComponent
import dev.kmpilot.linux.ui.App
import dev.kmpilot.linux.ui.buildRoot
import kotlinx.browser.document
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * A second REAL functioning KMPilot app — a port of LinuxCommandLibrary's core (offline command reference).
 * Renders the same way a KMP Android/iOS app would; runs live in the editor (the catalog's `preview` entry).
 */
@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    val scope = CoroutineScope(Dispatchers.Main)
    val root = buildRoot(scope)
    startBridgePosting() // stream the screen graph + live state to the editor (wasm preview only)
    startNavBridge(scope, root) // accept navigate commands from the editor (click-to-navigate)
    ComposeViewport(document.body!!) { App(root) }
}

private fun startBridgePosting() {
    js("setInterval(function(){ try { if (window.parent && window.parent !== window) { window.parent.postMessage({ type: 'kmpilot', appGraph: globalThis.__appGraph, currentScreen: globalThis.__currentScreen, chartSpec: globalThis.__chartSpec, screen: globalThis.__screen }, '*'); } } catch (e) {} }, 400)")
}

private fun startNavBridge(scope: CoroutineScope, root: RootComponent) {
    installNavListener()
    scope.launch {
        while (true) {
            delay(120)
            val cmd = readPendingNav()
            if (cmd.isNotEmpty()) { clearPendingNav(); root.navigateTo(cmd) }
        }
    }
}

// each js(...) must be a function's sole statement in Kotlin/Wasm
private fun installNavListener() {
    js("window.addEventListener('message', function(e){ if (e.data && e.data.type === 'kmpilot-cmd' && e.data.navigate) { globalThis.__pendingNav = e.data.navigate; } })")
}

private fun readPendingNav(): String = js("(globalThis.__pendingNav || '')")
private fun clearPendingNav() { js("globalThis.__pendingNav = null") }
