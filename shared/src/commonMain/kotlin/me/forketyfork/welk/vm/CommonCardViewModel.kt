package me.forketyfork.welk.vm

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch
import me.forketyfork.welk.domain.Card
import me.forketyfork.welk.domain.CardRepository
import me.forketyfork.welk.domain.Deck
import me.forketyfork.welk.domain.DeckRepository
import me.forketyfork.welk.presentation.CardAction

interface CardViewModel {
    val isFlipped: StateFlow<Boolean>
    val isEditing: StateFlow<Boolean>
    val currentCard: StateFlow<Card>
    val editCardContent: StateFlow<Pair<String, String>>
    val currentDeck: StateFlow<Deck?>
    val availableDecks: StateFlow<List<Deck>>

    fun flipCard()
    suspend fun nextCard()
    suspend fun loadDecks()
    suspend fun selectDeck(deckId: String)
    fun processAction(action: CardAction): Boolean
    suspend fun nextCardOnAnimationCompletion()
    fun updateEditContent(front: String, back: String)
    suspend fun saveCardEdit()
}

open class CommonCardViewModel(
    private val cardRepository: CardRepository,
    private val deckRepository: DeckRepository,
    private val cardAnimationManager: CardAnimationManager
) : CardViewModel {
    // Current position within the deck
    private val _currentCardPosition = MutableStateFlow(0)

    // Current selected deck
    private val _currentDeck = MutableStateFlow<Deck?>(null)
    override val currentDeck: StateFlow<Deck?> = _currentDeck.asStateFlow()

    // List of available decks
    private val _availableDecks = MutableStateFlow<List<Deck>>(emptyList())
    override val availableDecks: StateFlow<List<Deck>> = _availableDecks.asStateFlow()

    // List of cards in the current deck (cached to avoid repeated Firestore queries)
    private val _currentDeckCards = MutableStateFlow<List<Card>>(emptyList())

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
            // Load all decks initially
            loadDecks()

            // When deck changes, load its cards
            _currentDeck.collect { deck ->
                if (deck != null) {
                    _currentDeckCards.value = cardRepository.getCardsByDeckId(deck.id)
                    // Reset to the first card in the deck
                    _currentCardPosition.value = 0
                    updateCurrentCardFromPosition()
                }
            }
        }

        // Observe position changes and load the card
        kotlinx.coroutines.MainScope().launch {
            _currentCardPosition.collect { position ->
                updateCurrentCardFromPosition()
            }
        }
    }

    /**
     * Fetches the current card based on position in the current deck
     */
    private fun updateCurrentCardFromPosition() {
        val deck = _currentDeck.value ?: return
        val cards = _currentDeckCards.value

        if (cards.isEmpty()) {
            _currentCard.value = Card(deckId = deck.id)
            return
        }

        val position = _currentCardPosition.value.coerceIn(0, cards.size - 1)
        _currentCard.value = cards.getOrNull(position) ?: Card(deckId = deck.id)
    }

    /**
     * Loads all available decks
     */
    override suspend fun loadDecks() {
        try {
            val decks = deckRepository.getAllDecks()
            _availableDecks.value = decks

            // If we don't have a selected deck yet but decks exist, select the first one
            if (_currentDeck.value == null && decks.isNotEmpty()) {
                selectDeck(decks.first().id)
            }
        } catch (e: Exception) {
            // If there's an error loading decks, set to empty list but don't crash
            println("Error loading decks: ${e.message}")
            _availableDecks.value = emptyList()
        }
    }

    /**
     * Selects a deck by its ID and loads its cards
     */
    override suspend fun selectDeck(deckId: String) {
        try {
            val deck = deckRepository.getDeckById(deckId)
            _currentDeck.value = deck
            _currentDeckCards.value = cardRepository.getCardsByDeckId(deckId)
            _currentCardPosition.value = 0
            _isFlipped.value = false
        } catch (e: Exception) {
            // If there's an error selecting a deck, keep the current deck
            println("Error selecting deck: ${e.message}")
        }
    }

    override fun flipCard() {
        _isFlipped.value = _isFlipped.value.not()
    }

    override suspend fun nextCard() {
        val cardCount = _currentDeckCards.value.size
        if (cardCount > 0) {
            _currentCardPosition.value = (_currentCardPosition.value + 1) % cardCount
            _isFlipped.value = false
        }
    }

    override suspend fun nextCardOnAnimationCompletion() {
        cardAnimationManager.animationCompleteTrigger
            .filter { it.idx != -1 } // skipping initial value
            .collect {
                val currentCard = _currentCard.value
                // Update the learned status of the current card
                cardRepository.updateCardLearnedStatus(
                    currentCard.id,
                    currentCard.deckId,
                    it.learned
                )
                // Move to the next card
                nextCard()
                // Reset the animation trigger
                cardAnimationManager.reset()
            }
    }

    override fun updateEditContent(front: String, back: String) {
        _editCardContent.value = Pair(front, back)
    }

    override suspend fun saveCardEdit() {
        val (front, back) = _editCardContent.value
        val currentCard = _currentCard.value

        // Update card content in repository
        cardRepository.updateCardContent(currentCard.id, currentCard.deckId, front, back)

        // Update our local state with the changes
        val updatedCard = currentCard.copy(front = front, back = back)
        _currentCard.value = updatedCard

        // Also update the cached list of cards
        val currentPosition = _currentCardPosition.value
        val updatedCards = _currentDeckCards.value.toMutableList()
        if (currentPosition < updatedCards.size) {
            updatedCards[currentPosition] = updatedCard
            _currentDeckCards.value = updatedCards
        }
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
                    cardAnimationManager.swipeRight(_currentCardPosition.value)
                    true
                } else false
            }

            CardAction.SwipeLeft -> {
                if (!_isEditing.value) {
                    cardAnimationManager.swipeLeft(_currentCardPosition.value)
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