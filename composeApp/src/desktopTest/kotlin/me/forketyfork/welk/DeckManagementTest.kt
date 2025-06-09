@file:OptIn(InternalComposeUiApi::class)

package me.forketyfork.welk

import androidx.compose.ui.InternalComposeUiApi
import androidx.compose.ui.test.*
import me.forketyfork.welk.components.CardPanelTestTags
import me.forketyfork.welk.components.DeckItemTestTags
import me.forketyfork.welk.components.SidePanelTestTags
import org.junit.Test
import org.koin.test.KoinTest

class DeckManagementTest : KoinTest {

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun canCreateAndDeleteDeck() = runComposeUiTest {

        // Get test credentials and set up the app
        val (testUsername, testPassword) = getTestCredentials()
        setupApp()

        // Log in and verify basic UI elements
        login(testUsername, testPassword)
        verifyBasicUiElements()

        // Wait until the initial decks are loaded
        val preloadedDecks = mapOf(
            "deck1" to "Basic Vocabulary",
            "deck2" to "Grammar Rules",
            "deck3" to "Idioms"
        )

        preloadedDecks.forEach { (deckId, name) ->
            waitUntilExactlyOneExists(
                hasTestTag(DeckItemTestTags.DECK_NAME_TEMPLATE.format(deckId)),
                timeoutMillis = 10000
            )
            waitUntilExactlyOneExists(hasTextExactly(name))
        }

        // Click the add deck button
        onNodeWithTag(SidePanelTestTags.ADD_DECK_BUTTON).performClick()

        // wait until the new deck dialog opens
        waitUntilExactlyOneExists(hasTestTag(SidePanelTestTags.NEW_DECK_NAME))

        // Fill in the deck creation dialog
        onNodeWithTag(SidePanelTestTags.NEW_DECK_NAME).performTextInput("Test Deck")

        onNodeWithTag(SidePanelTestTags.NEW_DECK_DESCRIPTION).performTextInput("A test deck for verification")

        // Save the new deck
        onNodeWithTag(SidePanelTestTags.SAVE_DECK_BUTTON).performClick()

        // Wait for the new deck to appear
        waitUntilExactlyOneExists(hasTextExactly("Test Deck"))

        // Obtain the newly created deck id from its test tag
        val newDeckId = getDeckIdByName("Test Deck")

        // Select the new deck
        onNodeWithTag(DeckItemTestTags.DECK_NAME_TEMPLATE.format(newDeckId)).performClick()

        // Wait for the deck to be selected and verify no cards are shown initially
        waitUntilExactlyOneExists(hasText("No cards in this deck"))

        // Add a card to the new deck using its test tag
        onNodeWithTag(DeckItemTestTags.ADD_CARD_BUTTON_TEMPLATE.format(newDeckId)).performClick()

        waitUntilExactlyOneExists(hasTestTag(CardPanelTestTags.EDIT_FRONT))

        // Enter front text of the new card
        onNodeWithTag(CardPanelTestTags.EDIT_FRONT).performTextInput("Test Front")

        // Enter back text of the new card
        onNodeWithTag(CardPanelTestTags.EDIT_BACK).performTextInput("Test Back")

        // Save the new card
        onNodeWithTag(CardPanelTestTags.EDIT_SAVE).performClick()

        // Verify that the new card is added and visible
        waitUntilExactlyOneExists(hasTextExactly("Test Front"))

        // Verify that the deck now shows 1 card
        waitUntilExactlyOneExists(hasTextExactly("1 cards, 0 learned"))
        waitUntilExactlyOneExists(hasTextExactly("A test deck for verification"))

        // Delete the deck via the delete button tagged with the deck id
        onNodeWithTag(DeckItemTestTags.DELETE_DECK_BUTTON_TEMPLATE.format(newDeckId)).performClick()

        // Confirm the deletion in the dialog
        waitUntilExactlyOneExists(hasText("Are you sure you want to delete this deck? This action cannot be undone."))
        onNodeWithTag(SidePanelTestTags.CONFIRM_DELETE_BUTTON).performClick()

        // Verify that the deck is no longer visible
        waitUntilDoesNotExist(hasTextExactly("Test Deck"))

        // Verify that we're back to the original 3 decks
        preloadedDecks.forEach { (deckId, name) ->
            waitUntilExactlyOneExists(
                hasTestTag(DeckItemTestTags.DECK_NAME_TEMPLATE.format(deckId)),
                timeoutMillis = 5000
            )
            waitUntilExactlyOneExists(hasTextExactly(name))
        }

        // Log out
        logout()
    }
}