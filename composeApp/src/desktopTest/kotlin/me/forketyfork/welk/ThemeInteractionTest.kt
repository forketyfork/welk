@file:OptIn(InternalComposeUiApi::class)

package me.forketyfork.welk

import androidx.compose.ui.InternalComposeUiApi
import androidx.compose.ui.test.*
import me.forketyfork.welk.components.SidePanelTestTags
import org.junit.Test
import org.koin.test.KoinTest

class ThemeInteractionTest : KoinTest {

    @Test
    fun canToggleThemeMode() = runComposeUiTest {

        // Get test credentials and set up the app with a clean database
        val (testUsername, testPassword) = getTestCredentials()
        setupAppWithCleanDatabase(this, testUsername, testPassword)

        // Only verify the app title since we don't need to check all UI elements for this test
        waitUntilExactlyOneExists(hasTestTag(SidePanelTestTags.APP_TITLE), timeoutMillis = 10000)

        // verify the theme toggle button exists
        onNodeWithTag(SidePanelTestTags.THEME_TOGGLE_BUTTON).assertExists()

        // verify the initial theme is SYSTEM
        waitUntilExactlyOneExists(hasContentDescription("System theme (click to change)"), timeoutMillis = 5000)

        // click the theme toggle button to change to LIGHT theme
        onNodeWithTag(SidePanelTestTags.THEME_TOGGLE_BUTTON).performClick()

        // verify theme changed to LIGHT
        waitUntilExactlyOneExists(hasContentDescription("Light theme (click to change)"), timeoutMillis = 5000)

        // click the theme toggle button to change to DARK theme
        onNodeWithTag(SidePanelTestTags.THEME_TOGGLE_BUTTON).performClick()

        // verify theme changed to DARK
        waitUntilExactlyOneExists(hasContentDescription("Dark theme (click to change)"), timeoutMillis = 5000)

        // click the theme toggle button to change back to the SYSTEM theme
        onNodeWithTag(SidePanelTestTags.THEME_TOGGLE_BUTTON).performClick()

        // verify the theme changed back to SYSTEM
        waitUntilExactlyOneExists(hasContentDescription("System theme (click to change)"), timeoutMillis = 5000)

        // Log out
        logout()
    }
}
