@file:OptIn(InternalComposeUiApi::class)

package me.forketyfork.welk

import androidx.compose.ui.InternalComposeUiApi
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.runComposeUiTest
import androidx.compose.ui.test.waitUntilExactlyOneExists
import me.forketyfork.welk.components.SidePanelTestTags
import org.junit.Test
import org.koin.test.KoinTest

class ThemeInteractionTest : KoinTest {

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun canToggleThemeMode() = runComposeUiTest {

        // Get test credentials and set up the app with clean database
        val (testUsername, testPassword) = getTestCredentials()
        setupAppWithCleanDatabase(this, testUsername, testPassword)

        // Only verify the app title since we don't need to check all UI elements for this test
        waitUntilExactlyOneExists(hasTestTag(SidePanelTestTags.APP_TITLE), timeoutMillis = 10000)

        // verify theme toggle button exists
        onNodeWithTag(SidePanelTestTags.THEME_TOGGLE_BUTTON).assertExists()

        // verify initial theme is SYSTEM
        waitUntilExactlyOneExists(hasContentDescription("System theme (click to change)"), timeoutMillis = 5000)

        // click theme toggle button to change to LIGHT theme
        onNodeWithTag(SidePanelTestTags.THEME_TOGGLE_BUTTON).performClick()

        // verify theme changed to LIGHT
        waitUntilExactlyOneExists(hasContentDescription("Light theme (click to change)"), timeoutMillis = 5000)

        // click theme toggle button to change to DARK theme
        onNodeWithTag(SidePanelTestTags.THEME_TOGGLE_BUTTON).performClick()

        // verify theme changed to DARK
        waitUntilExactlyOneExists(hasContentDescription("Dark theme (click to change)"), timeoutMillis = 5000)

        // click theme toggle button to change back to SYSTEM theme
        onNodeWithTag(SidePanelTestTags.THEME_TOGGLE_BUTTON).performClick()

        // verify theme changed back to SYSTEM
        waitUntilExactlyOneExists(hasContentDescription("System theme (click to change)"), timeoutMillis = 5000)

        // Log out
        logout()
    }
}
