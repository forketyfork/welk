package me.forketyfork.welk

import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.hasTextExactly
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.runComposeUiTest
import androidx.compose.ui.test.waitUntilDoesNotExist
import androidx.compose.ui.test.waitUntilExactlyOneExists
import me.forketyfork.welk.components.DeckItemTestTags
import me.forketyfork.welk.components.SidePanelTestTags
import org.junit.Test
import org.koin.test.KoinTest

class NestedDeckTest : KoinTest {
    @Test
    fun canCreateAndExpandNestedDecks() =
        runComposeUiTest {
            // Get test credentials and set up the app with a clean database
            val (testUsername, testPassword) = getTestCredentials()
            setupAppWithCleanDatabase(this, testUsername, testPassword)

            var parentDeckId: String? = null

            try {
                // Create a top-level test deck using the utility function
                parentDeckId = createTestDeck("Test Parent Deck", "A test parent deck")

                // Add a child deck to the parent deck
                onNodeWithTag(DeckItemTestTags.ADD_DECK_BUTTON_TEMPLATE.format(parentDeckId)).performClick()
                waitUntilExactlyOneExists(hasTestTag(SidePanelTestTags.NEW_DECK_NAME))
                onNodeWithTag(SidePanelTestTags.NEW_DECK_NAME).performTextInput("Test Child Deck")
                onNodeWithTag(SidePanelTestTags.SAVE_DECK_BUTTON).performClick()

                // Wait for the child deck to be created and the parent deck to be expanded
                waitUntilExactlyOneExists(hasTextExactly("Test Child Deck"), timeoutMillis = 10000)

                // Get the child deck ID
                val childDeckId = getDeckIdByName("Test Child Deck")

                // Add a grandchild deck to the child deck
                onNodeWithTag(DeckItemTestTags.ADD_DECK_BUTTON_TEMPLATE.format(childDeckId)).performClick()
                waitUntilExactlyOneExists(hasTestTag(SidePanelTestTags.NEW_DECK_NAME))
                onNodeWithTag(SidePanelTestTags.NEW_DECK_NAME).performTextInput("Test Grandchild Deck")
                onNodeWithTag(SidePanelTestTags.SAVE_DECK_BUTTON).performClick()

                // Wait for the grandchild deck to be created and the child deck to be expanded
                waitUntilExactlyOneExists(hasTextExactly("Test Grandchild Deck"), timeoutMillis = 10000)

                // Clean up - delete the parent deck (which should cascade delete all children)
                onNodeWithTag(DeckItemTestTags.DELETE_DECK_BUTTON_TEMPLATE.format(parentDeckId)).performClick()
                waitUntilExactlyOneExists(
                    hasText("Are you sure you want to delete this deck? This action cannot be undone."),
                )
                onNodeWithTag(SidePanelTestTags.CONFIRM_DELETE_BUTTON).performClick()

                // Verify that the decks are deleted
                waitUntilDoesNotExist(hasTextExactly("Test Parent Deck"))
                waitUntilDoesNotExist(hasTextExactly("Test Child Deck"))
                waitUntilDoesNotExist(hasTextExactly("Test Grandchild Deck"))
                parentDeckId = null // Mark as deleted
            } finally {
                // Clean up: delete the parent deck if it still exists
                parentDeckId?.let { deckId ->
                    deleteTestDeck(deckId)
                    waitUntilDoesNotExist(hasTextExactly("Test Parent Deck"))
                }
                logout()
            }
        }
}
