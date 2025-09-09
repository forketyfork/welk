package me.forketyfork.welk

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import java.awt.Dimension

fun main() {
    application {
        Window(
            onCloseRequest = ::exitApplication,
            title = "welk",
        ) {
            window.minimumSize = Dimension(1024, 800)
            App()
        }
    }
}
