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
        verifyBasicUiElements()

        // wait until the decks are loaded, verify their expected contents
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
            waitUntilExactlyOneExists(
                hasTestTag(DeckItemTestTags.ADD_CARD_BUTTON_TEMPLATE.format(deckId)),
                timeoutMillis = 10000
            )

            waitUntilExactlyOneExists(hasTextExactly(*texts))
        }

        // add a new card to the first deck
        onNodeWithTag(DeckItemTestTags.ADD_CARD_BUTTON_TEMPLATE.format("deck1")).performClick()

        // Enter front text of the new card
        onNodeWithText("Front").performTextInput("New Front Text")

        // Enter back text of the new card
        onNodeWithText("Back").performTextInput("New Back Text")

        // Save the new card
        onNodeWithTag(CardPanelTestTags.EDIT_SAVE).performClick()

        // Verify that the new card is added and visible
        waitUntilExactlyOneExists(hasTextExactly("New Front Text"))

        // press space
        onRoot().performKeyInput { pressKey(Key.Spacebar) }

        // verify that the back of the card appeared
        waitUntilExactlyOneExists(hasTextExactly("New Back Text"))

        // verify that the number of cards bumped to 4
        waitUntilExactlyOneExists(hasTextExactly("4 cards", "Basic Vocabulary", "Essential words for beginners"))

        // delete the card
        onNodeWithTag(CardPanelTestTags.DELETE_BUTTON).performClick()
        waitUntilExactlyOneExists(hasTestTag(CardPanelTestTags.CONFIRM_DELETE_BUTTON))
        onNodeWithTag(CardPanelTestTags.CONFIRM_DELETE_BUTTON).performClick()

        // the number of cards is back to 3
        waitUntilExactlyOneExists(hasTextExactly("3 cards", "Basic Vocabulary", "Essential words for beginners"))

        // Log out
        logout()
    }
}
