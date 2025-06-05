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
        val preloadedDeckIdsAndTexts = mapOf(
            "deck1" to arrayOf("3 cards", "Basic Vocabulary", "Essential words for beginners"),
            "deck2" to arrayOf("2 cards", "Grammar Rules", "Key grammar concepts"),
            "deck3" to arrayOf("2 cards", "Idioms", "Common expressions and idioms")
        )

        preloadedDeckIdsAndTexts.forEach { (deckId, texts) ->
            waitUntilExactlyOneExists(
                hasTestTag(DeckItemTestTags.DECK_NAME_TEMPLATE.format(deckId)),
                timeoutMillis = 10000
            )
            waitUntilExactlyOneExists(hasTextExactly(*texts))
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

        // Wait for the new deck to appear and verify its contents
        waitUntilExactlyOneExists(hasTextExactly("0 cards", "Test Deck", "A test deck for verification"))

        // Find the deck ID by looking for the deck with "Test Deck" name
        // Since we can't predict the exact deck ID, we'll use the add card button to add a card
        // First, we need to find the add card button for our new deck
        // We'll click on the deck first to select it
        onNodeWithText("Test Deck").performClick()

        // Wait for the deck to be selected and verify no cards are shown initially
        waitUntilExactlyOneExists(hasText("No cards in this deck"))

        // Now add a card to the new deck by finding the add card button
        // We need to find the add card button for the new deck
        onNode(hasText("Add Card").and(hasParent(hasText("Test Deck")))).performClick()

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
        waitUntilExactlyOneExists(hasTextExactly("1 cards", "Test Deck", "A test deck for verification"))

        // Now delete the deck by clicking the delete button
        // We need to find the delete button for our test deck
        onNode(hasText("Delete").and(hasParent(hasText("Test Deck")))).performClick()

        // Confirm the deletion in the dialog
        waitUntilExactlyOneExists(hasText("Are you sure you want to delete this deck? This action cannot be undone."))
        onNodeWithText(SidePanelTestTags.CONFIRM_DELETE_BUTTON).performClick()

        // Verify that the deck is no longer visible
        waitUntilDoesNotExist(hasTextExactly("1 cards", "Test Deck", "A test deck for verification"))

        // Verify that we're back to the original 3 decks
        preloadedDeckIdsAndTexts.forEach { (deckId, texts) ->
            waitUntilExactlyOneExists(
                hasTestTag(DeckItemTestTags.DECK_NAME_TEMPLATE.format(deckId)),
                timeoutMillis = 5000
            )
            waitUntilExactlyOneExists(hasTextExactly(*texts))
        }

        // Log out
        logout()
    }
}