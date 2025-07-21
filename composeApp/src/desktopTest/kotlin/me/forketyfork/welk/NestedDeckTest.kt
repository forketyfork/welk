package me.forketyfork.welk

import androidx.compose.ui.test.*
import me.forketyfork.welk.components.DeckItemTestTags
import me.forketyfork.welk.components.SidePanelTestTags
import org.junit.Test
import org.koin.test.KoinTest

class NestedDeckTest : KoinTest {

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun canCreateAndExpandNestedDecks() = runComposeUiTest {
        setupApp()

        // Log in and verify basic UI elements
        login("user@test", "password")

        var parentDeckId: String? = null
        var parentName = ""

        try {
            val ts = System.currentTimeMillis()
            parentName = "Test Parent Deck $ts"
            // Create a top-level test deck using the utility function
            parentDeckId = createTestDeck(parentName, "A test parent deck")

            // Add a child deck to the parent deck
            onNodeWithTag(DeckItemTestTags.ADD_DECK_BUTTON_TEMPLATE.format(parentDeckId)).performClick()
            waitUntilExactlyOneExists(hasTestTag(SidePanelTestTags.NEW_DECK_NAME))
            val childName = "Test Child Deck $ts"
            onNodeWithTag(SidePanelTestTags.NEW_DECK_NAME).performTextInput(childName)
            onNodeWithTag(SidePanelTestTags.SAVE_DECK_BUTTON).performClick()

            // Wait for the child deck to be created and the parent deck to be expanded
            waitUntilExactlyOneExists(hasTextExactly(childName), timeoutMillis = 10000)

            // Get the child deck ID
            val childDeckId = getDeckIdByName(childName)

            // Add a grandchild deck to the child deck
            onNodeWithTag(DeckItemTestTags.ADD_DECK_BUTTON_TEMPLATE.format(childDeckId)).performClick()
            waitUntilExactlyOneExists(hasTestTag(SidePanelTestTags.NEW_DECK_NAME))
            val grandchildName = "Test Grandchild Deck $ts"
            onNodeWithTag(SidePanelTestTags.NEW_DECK_NAME).performTextInput(grandchildName)
            onNodeWithTag(SidePanelTestTags.SAVE_DECK_BUTTON).performClick()

            // Wait for the grandchild deck to be created and the child deck to be expanded
            waitUntilExactlyOneExists(hasTextExactly(grandchildName), timeoutMillis = 10000)

            // Clean up - delete the parent deck (which should cascade delete all children)
            onNodeWithTag(DeckItemTestTags.DELETE_DECK_BUTTON_TEMPLATE.format(parentDeckId)).performClick()
            waitUntilExactlyOneExists(hasText("Are you sure you want to delete this deck? This action cannot be undone."))
            onNodeWithTag(SidePanelTestTags.CONFIRM_DELETE_BUTTON).performClick()

            // Verify that the decks are deleted
            waitUntilDoesNotExist(hasTextExactly(parentName))
            waitUntilDoesNotExist(hasTextExactly(childName))
            waitUntilDoesNotExist(hasTextExactly(grandchildName))
            parentDeckId = null // Mark as deleted

        } finally {
            // Clean up: delete the parent deck if it still exists
            parentDeckId?.let { deckId ->
                deleteTestDeck(deckId)
                waitUntilDoesNotExist(hasTextExactly(parentName))
            }
            logout()
        }
    }
}