package me.forketyfork.welk.vm

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch
import me.forketyfork.welk.domain.Card
import me.forketyfork.welk.domain.CardRepository
import me.forketyfork.welk.presentation.CardAction

interface CardViewModel {
    val isFlipped: StateFlow<Boolean>
    val isEditing: StateFlow<Boolean>
    val currentCard: StateFlow<Card>
    val editCardContent: StateFlow<Pair<String, String>>
    fun flipCard()
    suspend fun nextCard()
    fun processAction(action: CardAction): Boolean
    suspend fun nextCardOnAnimationCompletion()
    fun updateEditContent(front: String, back: String)
    suspend fun saveCardEdit()
}

open class CommonCardViewModel(
    private val repository: CardRepository,
    private val cardAnimationManager: CardAnimationManager
) : CardViewModel {
    private val _currentCardIndex = MutableStateFlow(0)

    private val _isFlipped = MutableStateFlow(false)
    override val isFlipped: StateFlow<Boolean> = _isFlipped.asStateFlow()

    private val _isEditing = MutableStateFlow(false)
    override val isEditing: StateFlow<Boolean> = _isEditing.asStateFlow()

    private val _editCardContent = MutableStateFlow(Pair("", ""))
    override val editCardContent: StateFlow<Pair<String, String>> = _editCardContent.asStateFlow()

    private val _currentCard = MutableStateFlow(Card())
    override val currentCard: StateFlow<Card> = _currentCard.asStateFlow()

    init {
        // Use coroutine scope from the platform
        kotlinx.coroutines.MainScope().launch {
            // Observe card index changes and load the card
            _currentCardIndex.collect { index ->
                val card = repository.getByIndex(index)
                _currentCard.value = card
            }
        }
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

    override fun updateEditContent(front: String, back: String) {
        _editCardContent.value = Pair(front, back)
    }

    override suspend fun saveCardEdit() {
        val (front, back) = _editCardContent.value
        repository.updateCardContent(_currentCardIndex.value, front, back)

        // Update our local state with the changes
        val updatedCard = _currentCard.value.copy(front = front, back = back)
        _currentCard.value = updatedCard

        // This ensures the UI reflects the changes immediately
        // regardless of when the repository's data change is visible
    }

    override fun processAction(action: CardAction): Boolean {
        return when (action) {
            CardAction.Flip -> {
                if (!_isEditing.value) {
                    flipCard()
                    true
                } else false
            }

            CardAction.SwipeRight -> {
                if (!_isEditing.value) {
                    cardAnimationManager.swipeRight(_currentCardIndex.value)
                    true
                } else false
            }

            CardAction.SwipeLeft -> {
                if (!_isEditing.value) {
                    cardAnimationManager.swipeLeft(_currentCardIndex.value)
                    true
                } else false
            }

            CardAction.Edit -> {
                _isEditing.value = true
                val card = _currentCard.value
                _editCardContent.value = Pair(card.front, card.back)
                true
            }

            CardAction.SaveEdit -> {
                _isEditing.value = false
                true
            }

            CardAction.CancelEdit -> {
                _isEditing.value = false
                // Reset edit content to current card values to ensure next edit shows current values
                _editCardContent.value = Pair(_currentCard.value.front, _currentCard.value.back)
                true
            }

            CardAction.NoAction -> false
        }
    }

}
