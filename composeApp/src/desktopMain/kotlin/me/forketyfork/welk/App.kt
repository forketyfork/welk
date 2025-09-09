package me.forketyfork.welk

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.zIndex
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import me.forketyfork.welk.components.CardPanel
import me.forketyfork.welk.components.LoginView
import me.forketyfork.welk.components.SidePanel
import me.forketyfork.welk.di.appModule
import me.forketyfork.welk.theme.AppTheme
import me.forketyfork.welk.vm.DesktopLoginViewModel
import org.koin.compose.KoinApplication
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.module.Module

@Composable
fun App(
    modifier: Modifier = Modifier,
    module: Module = appModule,
) {
    KoinApplication(
        application = {
            modules(module)
        },
    ) {
        val loginViewModel = koinViewModel<DesktopLoginViewModel>()

        // userId is null if the user is not logged in
        val userId = loginViewModel.userId.collectAsStateWithLifecycle()
        var sidePanelWidth by remember { mutableIntStateOf(250) }

        AppTheme {
            if (userId.value == null) {
                // show the login screen
                Box(
                    modifier =
                        modifier
                            .fillMaxSize()
                            .background(AppTheme.colors.transparent),
                    contentAlignment = Alignment.Center,
                ) {
                    LoginView()
                }
            } else {
                // show the main application screen
                // Use Box as parent to manage z-index
                Box(
                    modifier =
                        modifier
                            .fillMaxSize()
                            .background(AppTheme.colors.transparent),
                ) {
                    // Use Row for the layout
                    Row(modifier = Modifier.fillMaxSize()) {
                        // Left panel with the app name and deck list
                        SidePanel(
                            modifier = Modifier.zIndex(10f), // Keep above other content
                            width = sidePanelWidth,
                            onWidthChange = { sidePanelWidth = it },
                        )

                        // Spacer to push the card panel to the center
                        Spacer(modifier = Modifier.weight(1f))

                        // The main content area with the card should get the focus by default
                        CardPanel(
                            // Use weight to center it
                            modifier = Modifier.weight(2f),
                        )

                        // Spacer to center the card
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}
