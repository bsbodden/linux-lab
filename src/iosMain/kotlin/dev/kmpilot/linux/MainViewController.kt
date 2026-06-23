package dev.kmpilot.linux

import androidx.compose.ui.window.ComposeUIViewController
import dev.kmpilot.linux.ui.App
import dev.kmpilot.linux.ui.buildRoot
import platform.UIKit.UIViewController

/** iOS entrypoint — the iosApp Xcode project hosts this in a SwiftUI UIViewControllerRepresentable. */
fun MainViewController(): UIViewController = ComposeUIViewController { App(buildRoot()) }
