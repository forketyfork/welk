package me.forketyfork.welk

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.zIndex
import androidx.lifecycle.viewmodel.compose.viewModel
import me.forketyfork.welk.ui.CardPanel
import me.forketyfork.welk.ui.SidePanel
import me.forketyfork.welk.ui.theme.AppTheme

@Composable
fun App(
    mainViewModel: MainViewModel = viewModel(),
    cardInteractionManager: CardInteractionManager = DefaultCardInteractionManager()
) {

    AppTheme {
        // Use Box as parent to manage z-index
        Box(modifier = Modifier.fillMaxSize()) {
            // Use Row for the layout
            Row(modifier = Modifier.fillMaxSize()) {
                // Left panel with the app name and deck list with fixed width
                SidePanel(
                    mainViewModel = mainViewModel,
                    // Fixed width instead of weight
                    modifier = Modifier.zIndex(10f) // Higher z-index to stay on top
                )

                // Spacer to push card panel to the center
                Spacer(modifier = Modifier.weight(1f))

                // Main content area with the card - should get default focus
                CardPanel(
                    mainViewModel = mainViewModel,
                    cardInteractionManager = cardInteractionManager,
                    // Use weight to center it
                    modifier = Modifier.weight(2f)
                )

                // Spacer to center the card
                Spacer(modifier = Modifier.weight(1f))
            }
        }
    }
}