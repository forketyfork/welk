package me.forketyfork.welk

import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.type

/**
 * Converts various interactions with the card (key and mouse events, swipes, etc.) into actions.
 */
interface CardInteractionManager {
    fun handleKeyEvent(keyEvent: KeyEvent): CardAction
}

sealed class CardAction {
    data object Flip : CardAction()
    data object SwipeRight : CardAction()
    data object SwipeLeft : CardAction()
    data object NoAction : CardAction()
}

class DefaultCardInteractionManager : CardInteractionManager {

    override fun handleKeyEvent(keyEvent: KeyEvent): CardAction {
        if (keyEvent.type != KeyEventType.KeyUp) {
            return CardAction.NoAction
        }
        return when (keyEvent.key) {
            Key.Spacebar -> CardAction.Flip
            Key.DirectionRight -> CardAction.SwipeRight
            Key.DirectionLeft -> CardAction.SwipeLeft
            else -> CardAction.NoAction
        }
    }

}
