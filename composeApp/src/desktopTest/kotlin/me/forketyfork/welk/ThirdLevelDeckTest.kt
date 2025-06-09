package me.forketyfork.welk

import androidx.compose.ui.test.*
import me.forketyfork.welk.components.DeckItemTestTags
import me.forketyfork.welk.components.SidePanelTestTags
import org.junit.Test
import org.koin.test.KoinTest

class ThirdLevelDeckTest : KoinTest {

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun testThirdLevelDeckVisibility() = runComposeUiTest {
        // This test verifies that third-level decks are visible in the UI
        val timestamp = System.currentTimeMillis()
        val level1Name = "Level 1 ($timestamp)"
        val level2Name = "Level 2 ($timestamp)"
        val level3Name = "Level 3 ($timestamp)"

        // Get test credentials and set up the app
        val (testUsername, testPassword) = getTestCredentials()
        setupApp()

        // Log in
        login(testUsername, testPassword)

        var level1DeckId: String? = null

        try {
            // Create a top-level deck using the createTestDeck utility
            level1DeckId = createTestDeck(level1Name, "Level 1 description")

            // Create a second-level deck
            onNodeWithTag(DeckItemTestTags.ADD_DECK_BUTTON_TEMPLATE.format(level1DeckId)).performClick()
            waitUntilExactlyOneExists(hasTestTag(SidePanelTestTags.NEW_DECK_NAME))
            onNodeWithTag(SidePanelTestTags.NEW_DECK_NAME).performTextInput(level2Name)
            onNodeWithTag(SidePanelTestTags.NEW_DECK_DESCRIPTION).performTextInput("Level 2 description")
            onNodeWithTag(SidePanelTestTags.SAVE_DECK_BUTTON).performClick()

            // Wait for the second-level deck to appear
            waitUntilExactlyOneExists(hasTextExactly(level2Name))

            // Get the second-level deck ID
            val level2DeckId = getDeckIdByName(level2Name)

            // Create a third-level deck
            onNodeWithTag(DeckItemTestTags.ADD_DECK_BUTTON_TEMPLATE.format(level2DeckId)).performClick()
            waitUntilExactlyOneExists(hasTestTag(SidePanelTestTags.NEW_DECK_NAME))
            onNodeWithTag(SidePanelTestTags.NEW_DECK_NAME).performTextInput(level3Name)
            onNodeWithTag(SidePanelTestTags.SAVE_DECK_BUTTON).performClick()

            // Wait for the third-level deck to appear
            waitUntilExactlyOneExists(hasTextExactly(level3Name))

            // Clean up - delete the top-level deck (which should cascade delete all children)
            deleteTestDeck(level1DeckId)
            level1DeckId = null // Mark as deleted

        } finally {
            // Cleanup in case of test failure
            level1DeckId?.let { deckId ->
                try {
                    deleteTestDeck(deckId)
                } catch (e: Exception) {
                    println("Failed to clean up deck $deckId: ${e.message}")
                    e.printStackTrace()
                }
            }

            // Log out
            try {
                logout()
            } catch (e: Exception) {
                println("Failed to logout: ${e.message}")
                e.printStackTrace()
            }
        }
    }
}
