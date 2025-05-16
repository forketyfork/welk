@file:OptIn(InternalComposeUiApi::class)

package me.forketyfork.welk

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.InternalComposeUiApi
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasTextExactly
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performKeyInput
import androidx.compose.ui.test.pressKey
import androidx.compose.ui.test.runComposeUiTest
import androidx.compose.ui.test.waitUntilDoesNotExist
import androidx.compose.ui.test.waitUntilExactlyOneExists
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.compose.LocalLifecycleOwner
import kotlinx.coroutines.runBlocking
import me.forketyfork.welk.domain.FirestoreRepository
import me.forketyfork.welk.ui.CardPanelTestTags
import me.forketyfork.welk.ui.DeckItemTestTags
import me.forketyfork.welk.ui.SidePanelTestTags
import org.junit.Test

class CardInteractionTest {

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun canViewAndFlipCards() = runComposeUiTest {

        // database cleanup
        runBlocking {
            val repo = FirestoreRepository()
            repo.getAllDecks().forEach { deck -> repo.deleteDeck(deck.id) }
        }

        val cardViewModel = DesktopCardViewModel()

        setContent {
            CompositionLocalProvider(
                LocalLifecycleOwner provides LocalLifecycleOwnerFake(),
            ) {
                App(cardViewModel = cardViewModel)
            }
        }

        // check basic UI elements are visible
        onNodeWithTag(SidePanelTestTags.APP_TITLE).assertTextEquals("Welk\uD83C\uDF42")
        onNodeWithTag(SidePanelTestTags.DECK_LIST_TITLE).assertTextEquals("Decks")

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
    }
}

/**
 * Fake implementation of [LifecycleOwner] to be used in the tests
 */
private class LocalLifecycleOwnerFake : LifecycleOwner {
    override val lifecycle: Lifecycle = LifecycleRegistry(this).apply {
        currentState = Lifecycle.State.RESUMED
    }
}

@Suppress("unused") // a handy function for debugging
@OptIn(ExperimentalTestApi::class)
private fun ComposeUiTest.printSemanticNodeState() {
    println()
    onAllNodes(SemanticsMatcher("all nodes") { true }).fetchSemanticsNodes().forEach { node ->
        println(
            "Node: Text = ${node.config.getOrNull(SemanticsProperties.Text)}, " +
                    "Tag = ${node.config.getOrNull(SemanticsProperties.TestTag)}, " +
                    "Role = ${node.config.getOrNull(SemanticsProperties.Role)}, " +
                    "Description = ${node.config.getOrNull(SemanticsProperties.ContentDescription)}, " +
                    "Focused = ${node.config.getOrNull(SemanticsProperties.Focused)}"
        )
    }
}
