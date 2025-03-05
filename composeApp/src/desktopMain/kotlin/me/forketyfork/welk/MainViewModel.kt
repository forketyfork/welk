package me.forketyfork.welk

import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch

class MainViewModel(val cardAnimationManager: CardAnimationManager = CardAnimationManager()) :
    ViewModel() {

    private val cardStore = CardStore()

    private val idx = mutableStateOf(0)
    val isFlipped = mutableStateOf(false)

    val currentCard = derivedStateOf {
        cardStore.cards[idx.value]
    }

    init {
        viewModelScope.launch {
            cardAnimationManager.animationCompleteTrigger
                .filter { it } // Only react when trigger is true
                .collect {
                    nextCard()
                    // Reset the trigger
                    cardAnimationManager.reset()
                }
        }
    }

    fun processAction(action: CardAction): Boolean {
        return when (action) {
            CardAction.Flip -> {
                isFlipped.value = isFlipped.value.not()
                true
            }

            CardAction.SwipeRight -> {
                cardAnimationManager.swipeRight()
                true
            }

            CardAction.SwipeLeft -> {
                cardAnimationManager.swipeLeft()
                true
            }

            CardAction.NoAction -> false
        }
    }

    private fun nextCard() {
        idx.value = idx.value.inc() % cardStore.cards.size
    }

}
