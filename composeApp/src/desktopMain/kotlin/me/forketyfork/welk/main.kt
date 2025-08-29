package me.forketyfork.welk

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application

fun main() {
    application {
        Window(
            onCloseRequest = ::exitApplication,
            title = "welk",
        ) {
            window.minimumSize = java.awt.Dimension(1024, 800)
            App()
        }
    }
}