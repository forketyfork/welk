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
    val isNewCard: StateFlow<Boolean>

    fun flipCard()
    suspend fun nextCard()
    suspend fun loadDecks()
    suspend fun selectDeck(deckId: String)
    fun processAction(action: CardAction): Boolean
    suspend fun nextCardOnAnimationCompletion()
    fun updateEditContent(front: String, back: String)
    suspend fun saveCardEdit()
    suspend fun createNewCard(deckId: String)
    suspend fun cancelNewCard()
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

    private val _isNewCard = MutableStateFlow(false)
    override val isNewCard: StateFlow<Boolean> = _isNewCard.asStateFlow()

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
                // Clear the new card flag when saved
                if (_isNewCard.value) {
                    _isNewCard.value = false
                }
                true
            }

            CardAction.CancelEdit -> {
                _isEditing.value = false

                // If this is a new card being canceled, perform special handling
                if (_isNewCard.value) {
                    kotlinx.coroutines.MainScope().launch {
                        cancelNewCard()
                    }
                } else {
                    // Otherwise just reset edit content
                    _editCardContent.value = Pair(_currentCard.value.front, _currentCard.value.back)
                }
                true
            }

            is CardAction.CreateNewCard -> {
                kotlinx.coroutines.MainScope().launch {
                    createNewCard(action.deckId)
                }
                true
            }

            CardAction.NoAction -> false
        }
    }

    /**
     * Creates a new empty card in the specified deck and switches to it in edit mode
     */
    override suspend fun createNewCard(deckId: String) {
        // First select the deck if it's not already selected
        if (_currentDeck.value?.id != deckId) {
            selectDeck(deckId)
        }

        // Create a new card in the repository
        val newCard = cardRepository.createCard(deckId, "", "")

        // Set new card state
        _isNewCard.value = true

        // Update the current deck with increased card count
        val currentDeck = _currentDeck.value
        if (currentDeck != null) {
            val updatedDeck = currentDeck.copy(cardCount = currentDeck.cardCount + 1)
            _currentDeck.value = updatedDeck

            // Also update this deck in the available decks list
            val updatedDecks = _availableDecks.value.toMutableList()
            val deckIndex = updatedDecks.indexOfFirst { it.id == deckId }
            if (deckIndex != -1) {
                updatedDecks[deckIndex] = updatedDeck
                _availableDecks.value = updatedDecks
            }
        }

        // Update card list and position
        val updatedCards = _currentDeckCards.value.toMutableList()
        updatedCards.add(newCard)
        _currentDeckCards.value = updatedCards
        _currentCardPosition.value = updatedCards.size - 1
        _currentCard.value = newCard

        // Enter edit mode
        _editCardContent.value = Pair("", "")
        _isEditing.value = true
    }

    /**
     * Cancels a new card, removing it from the repository
     */
    override suspend fun cancelNewCard() {
        if (_isNewCard.value) {
            val card = _currentCard.value

            // Delete the card from the repository
            cardRepository.deleteCard(card.id, card.deckId)

            // Reset UI state
            _isNewCard.value = false
            _isEditing.value = false

            // Update the deck card count
            val currentDeck = _currentDeck.value
            if (currentDeck != null) {
                val updatedDeck = currentDeck.copy(cardCount = currentDeck.cardCount - 1)
                _currentDeck.value = updatedDeck

                // Also update this deck in the available decks list
                val updatedDecks = _availableDecks.value.toMutableList()
                val deckIndex = updatedDecks.indexOfFirst { it.id == card.deckId }
                if (deckIndex != -1) {
                    updatedDecks[deckIndex] = updatedDeck
                    _availableDecks.value = updatedDecks
                }
            }

            // Remove from local card list
            val updatedCards = _currentDeckCards.value.toMutableList()
            updatedCards.removeAt(_currentCardPosition.value)
            _currentDeckCards.value = updatedCards

            // Set position to the first card if available
            if (updatedCards.isNotEmpty()) {
                _currentCardPosition.value = 0
                _currentCard.value = updatedCards[0]
            } else {
                _currentCard.value = Card(deckId = card.deckId)
            }
        }
    }
}