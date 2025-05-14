package me.forketyfork.welk

import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.type
import me.forketyfork.welk.presentation.CardAction

/**
 * Converts various interactions with the card (key and mouse events, swipes, etc.) into actions.
 */
interface CardInteractionManager {
    fun handleKeyEvent(keyEvent: KeyEvent): CardAction
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
            Key.E -> CardAction.Edit
            Key.Enter -> CardAction.SaveEdit
            Key.Escape -> CardAction.CancelEdit
            Key.N -> CardAction.CreateNewCardInCurrentDeck
            else -> CardAction.NoAction
        }
    }

}
