@file:OptIn(InternalComposeUiApi::class)

package me.forketyfork.welk

import androidx.compose.ui.InternalComposeUiApi
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.test.*
import me.forketyfork.welk.components.CardPanelTestTags
import me.forketyfork.welk.components.DeckItemTestTags
import org.junit.Test
import org.koin.test.KoinTest

class CardInteractionTest : KoinTest {

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun canViewAndFlipCards() = runComposeUiTest {

        // Get test credentials and set up the app
        val (testUsername, testPassword) = getTestCredentials()
        setupApp()

        // Log in and verify basic UI elements
        login(testUsername, testPassword)
        verifyBasicUiElements()

        // wait until the decks are loaded, verify their expected contents
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
            waitUntilExactlyOneExists(
                hasTestTag(DeckItemTestTags.ADD_CARD_BUTTON_TEMPLATE.format(deckId)),
                timeoutMillis = 10000
            )
            waitUntilExactlyOneExists(hasTextExactly(name))
        }

        // wait until the first deck is selected and the card is loaded
        waitUntilExactlyOneExists(hasTestTag(CardPanelTestTags.CARD_PANEL))
        waitUntilExactlyOneExists(hasTextExactly("Hello"))

        // assert that the first card in the first deck is visible and has edit and delete buttons
        onNodeWithTag(CardPanelTestTags.EDIT_BUTTON).assertExists()
        onNodeWithTag(CardPanelTestTags.DELETE_BUTTON).assertExists()

        // check that the front of the card is visible but the back is not
        onNodeWithText("Hello").assertExists()
        onNodeWithText("Hola").assertDoesNotExist()

        // press space
        onRoot().performKeyInput { pressKey(Key.Spacebar) }

        // verify that the back of the card appeared
        waitUntilExactlyOneExists(hasTextExactly("Hola"))

        // press space again
        onRoot().performKeyInput { pressKey(Key.Spacebar) }

        // verify that the back of the card is hidden
        waitUntilDoesNotExist(hasTextExactly("Hola"))

        // Log out
        logout()
    }
}
