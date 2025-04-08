package me.forketyfork.welk.vm

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import me.forketyfork.welk.domain.Card
import me.forketyfork.welk.domain.CardRepository
import me.forketyfork.welk.presentation.CardAction

interface CardViewModel {
    val isFlipped: StateFlow<Boolean>
    val currentCard: Flow<Card>
    fun flipCard()
    suspend fun nextCard()
    fun processAction(action: CardAction): Boolean
    suspend fun nextCardOnAnimationCompletion()
}

open class CommonCardViewModel(
    private val repository: CardRepository,
    private val cardAnimationManager: CardAnimationManager
) : CardViewModel {
    private val _currentCardIndex = MutableStateFlow(0)

    private val _isFlipped = MutableStateFlow(false)
    override val isFlipped: StateFlow<Boolean> = _isFlipped.asStateFlow()

    override val currentCard: Flow<Card> = _currentCardIndex.map { index ->
        repository.getByIndex(index)
    }

    override fun flipCard() {
        _isFlipped.value = _isFlipped.value.not()
    }

    override suspend fun nextCard() {
        _currentCardIndex.value = (_currentCardIndex.value + 1) % repository.getCardCount()
        _isFlipped.value = false
    }

    override suspend fun nextCardOnAnimationCompletion() {
        cardAnimationManager.animationCompleteTrigger
            .filter { it.idx != -1 } // skipping initial value
            .collect {
                nextCard()
                // TODO error handling
                repository.updateCardLearnedStatus(it.idx, it.learned)
                // Reset the trigger
                cardAnimationManager.reset()
            }
    }

    override fun processAction(action: CardAction): Boolean {
        return when (action) {
            CardAction.Flip -> {
                flipCard()
                true
            }

            CardAction.SwipeRight -> {
                cardAnimationManager.swipeRight(_currentCardIndex.value)
                true
            }

            CardAction.SwipeLeft -> {
                cardAnimationManager.swipeLeft(_currentCardIndex.value)
                true
            }

            CardAction.NoAction -> false
        }
    }

}
