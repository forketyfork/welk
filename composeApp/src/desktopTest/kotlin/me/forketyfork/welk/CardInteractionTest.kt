package me.forketyfork.welk

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.InternalComposeUiApi
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.runComposeUiTest
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.compose.LocalLifecycleOwner
import me.forketyfork.welk.presentation.CardAction
import org.junit.Test
import kotlin.test.assertTrue

/**
 * Fake implementation of [LifecycleOwner] to be used in the tests
 */
private class LocalLifecycleOwnerFake : LifecycleOwner {
    override val lifecycle: Lifecycle = LifecycleRegistry(this).apply {
        currentState = Lifecycle.State.RESUMED
    }
}

class CardInteractionTest {

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun myTest() = runComposeUiTest {

        val testCardInteractionManager = TestCardInteractionManager()
        val mainViewModel = MainViewModel()
        setContent {
            CompositionLocalProvider(
                LocalLifecycleOwner provides LocalLifecycleOwnerFake(),
            ) {
                App(
                    mainViewModel = mainViewModel,
                    cardInteractionManager = testCardInteractionManager
                )
            }

            onNodeWithTag("app_title").assertTextEquals("Welk\uD83C\uDF42")

            // When the space bar is pressed
            testCardInteractionManager.simulateKeyPress(Key.Spacebar)

            // Then the card should be flipped
            assertTrue(
                mainViewModel.isFlipped.value,
                "Card should be flipped after pressing space bar"
            )

        }

    }
}

/**
 * Test implementation of CardInteractionManager that allows simulating key events
 */
class TestCardInteractionManager : DefaultCardInteractionManager() {

    @OptIn(InternalComposeUiApi::class)
    fun simulateKeyPress(key: Key): CardAction {
        // Create a synthetic key event and process it using the default manager logic
        return handleKeyEvent(
            KeyEvent(
                type = KeyEventType.KeyUp,
                key = key
            )
        )
    }

}
