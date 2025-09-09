@file:OptIn(InternalComposeUiApi::class)

package me.forketyfork.welk

import androidx.compose.ui.InternalComposeUiApi
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.hasTextExactly
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.runComposeUiTest
import androidx.compose.ui.test.waitUntilDoesNotExist
import androidx.compose.ui.test.waitUntilExactlyOneExists
import me.forketyfork.welk.components.CardPanelTestTags
import me.forketyfork.welk.components.DeckItemTestTags
import me.forketyfork.welk.components.SidePanelTestTags
import org.junit.Test
import org.koin.test.KoinTest

class DeckManagementTest : KoinTest {
    @OptIn(ExperimentalTestApi::class)
    @Test
    fun canCreateAndDeleteDeck() =
        runComposeUiTest {
            // Get test credentials and set up the app with clean database
            val (testUsername, testPassword) = getTestCredentials()
            setupAppWithCleanDatabase(this, testUsername, testPassword)

            var testDeckId: String? = null

            try {
                // Create some initial decks to verify the deck count changes properly
                val initialDeck1 = createTestDeck("Initial Deck 1", "First initial deck")
                val initialDeck2 = createTestDeck("Initial Deck 2", "Second initial deck")

                // Click the "Add deck" button to create the main test deck
                onNodeWithTag(SidePanelTestTags.ADD_DECK_BUTTON).performClick()

                // Wait until the new deck dialog opens
                waitUntilExactlyOneExists(hasTestTag(SidePanelTestTags.NEW_DECK_NAME))

                // Fill in the deck creation dialog
                onNodeWithTag(SidePanelTestTags.NEW_DECK_NAME).performTextInput("Test Deck")
                onNodeWithTag(SidePanelTestTags.NEW_DECK_DESCRIPTION).performTextInput("A test deck for verification")

                // Save the new deck
                onNodeWithTag(SidePanelTestTags.SAVE_DECK_BUTTON).performClick()

                // Wait for the new deck to appear
                waitUntilExactlyOneExists(hasTextExactly("Test Deck"))

                // Get the newly created deck id from its test tag
                testDeckId = getDeckIdByName("Test Deck")

                // Select the new deck
                onNodeWithTag(DeckItemTestTags.DECK_NAME_TEMPLATE.format(testDeckId)).performClick()

                // Wait for the deck to be selected and verify no cards are shown initially
                waitUntilExactlyOneExists(hasText("No cards in this deck"))

                // Add a card to the new deck using its test tag
                onNodeWithTag(DeckItemTestTags.ADD_CARD_BUTTON_TEMPLATE.format(testDeckId)).performClick()

                waitUntilExactlyOneExists(hasTestTag(CardPanelTestTags.EDIT_FRONT))

                // Enter the front text of the new card
                onNodeWithTag(CardPanelTestTags.EDIT_FRONT).performTextInput("Test Front")

                // Enter the back text of the new card
                onNodeWithTag(CardPanelTestTags.EDIT_BACK).performTextInput("Test Back")

                // Save the new card
                onNodeWithTag(CardPanelTestTags.EDIT_SAVE).performClick()

                // Verify that the new card is added and visible
                waitUntilExactlyOneExists(hasTextExactly("Test Front"))

                // Verify that the deck now shows 1 card
                waitUntilExactlyOneExists(hasTextExactly("1 cards, 0 learned"))
                waitUntilExactlyOneExists(hasTextExactly("A test deck for verification"))

                // Delete the deck via the delete button tagged with the deck id
                onNodeWithTag(DeckItemTestTags.DELETE_DECK_BUTTON_TEMPLATE.format(testDeckId)).performClick()

                // Confirm the deletion in the dialog
                waitUntilExactlyOneExists(
                    hasText("Are you sure you want to delete this deck? This action cannot be undone."),
                )
                onNodeWithTag(SidePanelTestTags.CONFIRM_DELETE_BUTTON).performClick()

                // Verify that the deck is no longer visible
                waitUntilDoesNotExist(hasTextExactly("Test Deck"))
                testDeckId = null // Mark as deleted

                // Verify that the initial decks are still there
                waitUntilExactlyOneExists(hasTextExactly("Initial Deck 1"))
                waitUntilExactlyOneExists(hasTextExactly("Initial Deck 2"))

                // Clean up initial decks
                deleteTestDeck(initialDeck1)
                deleteTestDeck(initialDeck2)

                // Verify initial decks are deleted
                waitUntilDoesNotExist(hasTextExactly("Initial Deck 1"))
                waitUntilDoesNotExist(hasTextExactly("Initial Deck 2"))
            } finally {
                // Clean up: delete the test deck if it still exists
                testDeckId?.let { deckId ->
                    deleteTestDeck(deckId)
                    waitUntilDoesNotExist(hasTextExactly("Test Deck"))
                }
                logout()
            }
        }
}
