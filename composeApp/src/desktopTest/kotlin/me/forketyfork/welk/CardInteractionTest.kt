@file:OptIn(InternalComposeUiApi::class)

package me.forketyfork.welk

import androidx.compose.ui.InternalComposeUiApi
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.test.*
import me.forketyfork.welk.components.CardPanelTestTags
import me.forketyfork.welk.components.DeckItemTestTags
import org.junit.Test
import org.koin.test.KoinTest

@OptIn(ExperimentalTestApi::class)
class CardInteractionTest : KoinTest {

    @Test
    fun canViewAndFlipCards() = runComposeUiTest {

        // Get test credentials and set up the app with a clean database
        val (testUsername, testPassword) = getTestCredentials()
        setupAppWithCleanDatabase(this, testUsername, testPassword)

        var testDeckId: String? = null

        try {
            // Create a test deck with a card
            testDeckId = createTestDeck("Test Deck", "Test deck for card interaction")

            // Create a test card in the deck
            createTestCard(testDeckId, "Hello", "Hola")

            // Select the test deck
            onNodeWithTag(DeckItemTestTags.DECK_NAME_TEMPLATE.format(testDeckId)).performClick()

            // Wait until the deck is selected and the card is loaded
            waitUntilExactlyOneExists(hasTestTag(CardPanelTestTags.CARD_PANEL))
            waitUntilExactlyOneExists(hasTextExactly("Hello"))

            // Assert that the card is visible and has edit and delete buttons
            onNodeWithTag(CardPanelTestTags.EDIT_BUTTON).assertExists()
            onNodeWithTag(CardPanelTestTags.DELETE_BUTTON).assertExists()

            // Check that the front of the card is visible but the back is not
            onNodeWithText("Hello").assertExists()
            onNodeWithText("Hola").assertDoesNotExist()

            // Press space to flip the card
            onRoot().performKeyInput { pressKey(Key.Spacebar) }

            // Verify that the back of the card appeared
            waitUntilExactlyOneExists(hasTextExactly("Hola"))

            // Press space again to flip back
            onRoot().performKeyInput { pressKey(Key.Spacebar) }

            // Verify that the back of the card is hidden
            waitUntilDoesNotExist(hasTextExactly("Hola"))

            // click on the edit button
            onNodeWithTag(CardPanelTestTags.EDIT_BUTTON).performClick()
            waitUntilExactlyOneExists(hasTestTag(CardPanelTestTags.EDIT_FRONT))

            // Input new values
            onNodeWithTag(CardPanelTestTags.EDIT_FRONT).performTextClearance()
            onNodeWithTag(CardPanelTestTags.EDIT_FRONT).performTextInput("Edited Front")
            onNodeWithTag(CardPanelTestTags.EDIT_BACK).performTextClearance()
            onNodeWithTag(CardPanelTestTags.EDIT_BACK).performTextInput("Edited Back")

            // save and wait for the value to update
            onNodeWithTag(CardPanelTestTags.EDIT_SAVE).performClick()
            waitUntilExactlyOneExists(hasTextExactly("Edited Front"))

            // verify flipped content works after edit
            onRoot().performKeyInput { pressKey(Key.Spacebar) }
            waitUntilExactlyOneExists(hasTextExactly("Edited Back"))

        } finally {
            // Clean up: delete the test deck if it was created
            testDeckId?.let { deckId ->
                deleteTestDeck(deckId)
                // Verify the deck is deleted
                waitUntilDoesNotExist(hasTextExactly("Test Deck"))
            }
            logout()
        }
    }

}
