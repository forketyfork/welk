package me.forketyfork.welk

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.hasTextExactly
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performKeyInput
import androidx.compose.ui.test.pressKey
import androidx.compose.ui.test.runComposeUiTest
import androidx.compose.ui.test.waitUntilDoesNotExist
import androidx.compose.ui.test.waitUntilExactlyOneExists
import me.forketyfork.welk.components.CardPanelTestTags
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

        // Select the first deck and add a new card
        onNodeWithTag(DeckItemTestTags.DECK_NAME_TEMPLATE.format("deck1")).performClick()
        onNodeWithTag(CardPanelTestTags.ADD_CARD_BUTTON).performClick()

        // Enter front text of the new card
        onNodeWithText("Front").performTextInput("New Front Text")

        // Enter back text of the new card
        onNodeWithText("Back").performTextInput("New Back Text")

        // Save the new card
        onNodeWithTag(CardPanelTestTags.SAVE_BUTTON).performClick()

        // Verify that the new card is added and visible
        waitUntilExactlyOneExists(hasTextExactly("New Front Text"))

        // Log out
        logout()
    }
}
