package me.forketyfork.welk

import androidx.compose.ui.input.key.Key
import androidx.compose.ui.test.*
import me.forketyfork.welk.components.CardPanelTestTags
import me.forketyfork.welk.components.DeckItemTestTags
import org.junit.Test
import org.koin.test.KoinTest

class CreateNewCardTest : KoinTest {

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun canCreateNewCard() = runComposeUiTest {

        // Get test credentials and set up the app
        val (testUsername, testPassword) = getTestCredentials()
        setupApp()

        // Log in and verify basic UI elements
        login(testUsername, testPassword)

        var testDeckId: String? = null

        try {
            // Create a test deck with some existing cards
            testDeckId = createTestDeck("Test Vocabulary", "Test deck for card creation")

            // Create some initial cards in the deck
            createTestCard(testDeckId, "Hello", "Hola")
            createTestCard(testDeckId, "Goodbye", "Adiós")
            createTestCard(testDeckId, "Thank you", "Gracias")

            // Select the test deck to show the cards
            onNodeWithTag(DeckItemTestTags.DECK_NAME_TEMPLATE.format(testDeckId)).performClick()

            // Wait until the deck is selected and verify card count
            waitUntilExactlyOneExists(hasTextExactly("3 cards, 0 learned"))
            waitUntilExactlyOneExists(hasTextExactly("Test deck for card creation"))

            // Add a new card to the deck
            onNodeWithTag(DeckItemTestTags.ADD_CARD_BUTTON_TEMPLATE.format(testDeckId)).performClick()

            waitUntilExactlyOneExists(hasTestTag(CardPanelTestTags.EDIT_FRONT))

            // Enter the front text of the new card
            onNodeWithTag(CardPanelTestTags.EDIT_FRONT).performTextInput("New Front Text")

            // Enter the back text of the new card
            onNodeWithTag(CardPanelTestTags.EDIT_BACK).performTextInput("New Back Text")

            // Save the new card
            onNodeWithTag(CardPanelTestTags.EDIT_SAVE).performClick()

            // Verify that the new card is added and visible
            waitUntilExactlyOneExists(hasTextExactly("New Front Text"))

            // Press space to flip the card
            onRoot().performKeyInput { pressKey(Key.Spacebar) }

            // Verify that the back of the card appeared
            waitUntilExactlyOneExists(hasTextExactly("New Back Text"))

            // Verify that the number of cards increased to 4
            waitUntilExactlyOneExists(hasTextExactly("4 cards, 0 learned"))
            waitUntilExactlyOneExists(hasTextExactly("Test deck for card creation"))

            // Delete the newly created card
            onNodeWithTag(CardPanelTestTags.DELETE_BUTTON).performClick()
            waitUntilExactlyOneExists(hasTestTag(CardPanelTestTags.CONFIRM_DELETE_BUTTON))
            onNodeWithTag(CardPanelTestTags.CONFIRM_DELETE_BUTTON).performClick()

            // Verify the number of cards is back to 3
            waitUntilExactlyOneExists(hasTextExactly("3 cards, 0 learned"))

        } finally {
            // Clean up: delete the test deck if it was created
            testDeckId?.let { deckId ->
                deleteTestDeck(deckId)
                // Verify the deck is deleted
                waitUntilDoesNotExist(hasTextExactly("Test Vocabulary"))
            }
            logout()
        }
    }
}
