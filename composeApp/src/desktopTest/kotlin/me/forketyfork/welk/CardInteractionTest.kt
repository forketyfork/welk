@file:OptIn(InternalComposeUiApi::class)

package me.forketyfork.welk

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.InternalComposeUiApi
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performKeyPress
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.runComposeUiTest
import androidx.compose.ui.test.waitUntilExactlyOneExists
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.compose.LocalLifecycleOwner
import me.forketyfork.welk.ui.CardPanelTestTags
import me.forketyfork.welk.ui.DeckItemTestTags
import me.forketyfork.welk.ui.SidePanelTestTags
import org.junit.Test

class CardInteractionTest {

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun createCardTest() = runComposeUiTest {

        val mainViewModel = MainViewModel()

        setContent {
            CompositionLocalProvider(
                LocalLifecycleOwner provides LocalLifecycleOwnerFake(),
            ) {
                App(mainViewModel = mainViewModel)
            }
        }

        onNodeWithTag(SidePanelTestTags.APP_TITLE).assertTextEquals("Welk\uD83C\uDF42")

        waitUntilExactlyOneExists(
            matcher = hasTestTag(DeckItemTestTags.DECK_NAME_TEMPLATE.format("deck1")),
            timeoutMillis = 5000
        )

        // simulate keypress on the N key
        onRoot().performKeyPress(
            KeyEvent(
                key = Key.N,
                type = KeyEventType.KeyUp
            )
        )

        // verify that the front and back inputs are empty
        onNodeWithTag(CardPanelTestTags.EDIT_FRONT).assertTextEquals("")
        onNodeWithTag(CardPanelTestTags.EDIT_BACK).assertTextEquals("")

        // enter values into the inputs
        onNodeWithTag(CardPanelTestTags.EDIT_FRONT).performTextInput("Hello")
        onNodeWithTag(CardPanelTestTags.EDIT_BACK).performTextInput("World")

        // click Save
        onNodeWithTag(CardPanelTestTags.EDIT_SAVE).performClick()

        // verify that the card was created
        onNodeWithTag(CardPanelTestTags.VIEW_FRONT).assertTextEquals("Hello")
        onNodeWithTag(CardPanelTestTags.VIEW_BACK).assertTextEquals("World")

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
