package me.forketyfork.welk.vm

import co.touchlab.kermit.Logger
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
    val isDeleteConfirmationShowing: StateFlow<Boolean>
    val hasCards: StateFlow<Boolean>

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
    suspend fun deleteCurrentCard()
    fun showDeleteConfirmation()
    fun hideDeleteConfirmation()
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

    private val _isDeleteConfirmationShowing = MutableStateFlow(false)
    override val isDeleteConfirmationShowing: StateFlow<Boolean> =
        _isDeleteConfirmationShowing.asStateFlow()

    private val _hasCards = MutableStateFlow(false)
    override val hasCards: StateFlow<Boolean> = _hasCards.asStateFlow()

    private val logger = Logger.withTag("CommonCardViewModel")

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

        // Update the hasCards state
        _hasCards.value = cards.isNotEmpty()

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

            logger.e(e) { "Error loading decks: ${e.message}" }
            _availableDecks.value = emptyList()
        }
    }

    /**
     * Selects a deck by its ID and loads its cards
     */
    override suspend fun selectDeck(deckId: String) {
        try {
            logger.d { "Selecting deck with ID: $deckId" }
            val deck = deckRepository.getDeckById(deckId)
            _currentDeck.value = deck

            // Load cards for this deck
            val cards = cardRepository.getCardsByDeckId(deckId)
            logger.d { "Loaded ${cards.size} cards for deck $deckId" }

            // Store the cards in our local cache
            _currentDeckCards.value = cards

            // Reset position and flip state
            _currentCardPosition.value = 0
            _isFlipped.value = false

            // Update hasCards state
            _hasCards.value = cards.isNotEmpty()

            logger.i { "Deck selected: ${deck.name}, card count: ${cards.size}" }
        } catch (e: Exception) {
            // If there's an error selecting a deck, keep the current deck
            logger.e(e) { "Error selecting deck: ${e.message}" }
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

        try {
            // If the content is empty, don't save (optional validation)
            if (front.isBlank() && back.isBlank()) {

                logger.w("Not saving card with empty content")
                if (_isNewCard.value) {
                    // If this is a new card with empty content, just cancel it
                    cancelNewCard()
                }
                return
            }

            // Check if we're saving a new card or updating an existing one
            if (_isNewCard.value || currentCard.id.isEmpty()) {
                logger.i("Creating a new card in the database")

                // Get the deck ID
                val deckId = currentCard.deckId.ifEmpty { _currentDeck.value?.id ?: return }

                // Create a new card in the repository with the edited content
                val newCard = cardRepository.createCard(
                    deckId = deckId,
                    front = front,
                    back = back
                )

                // Make sure the new card has an ID
                if (newCard.id.isEmpty()) {
                    logger.e { "New card was created but still has no ID" }
                    return
                }

                // Update the view model with the new card
                _currentCard.value = newCard

                // Add the new card to the card list
                val updatedCards = _currentDeckCards.value.toMutableList()
                updatedCards.add(newCard)
                _currentDeckCards.value = updatedCards
                _currentCardPosition.value = updatedCards.size - 1

                // Update hasCards state
                _hasCards.value = true

                // Mark as no longer a new card since it's been saved
                _isNewCard.value = false

                // Update the deck card count in the UI
                val currentDeck = _currentDeck.value
                if (currentDeck != null) {
                    val updatedDeck = currentDeck.copy(cardCount = updatedCards.size)
                    _currentDeck.value = updatedDeck

                    // Also update this deck in the available decks list
                    val updatedDecks = _availableDecks.value.toMutableList()
                    val deckIndex = updatedDecks.indexOfFirst { it.id == deckId }
                    if (deckIndex != -1) {
                        updatedDecks[deckIndex] = updatedDeck
                        _availableDecks.value = updatedDecks
                    }
                }

                return
            }

            // Normal update for an existing card with a valid ID
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
        } catch (e: Exception) {
            logger.e(e) { "Error saving card" }
        }
    }

    override fun processAction(action: CardAction): Boolean {
        return when (action) {
            CardAction.Flip -> {
                if (!_isEditing.value && !_isDeleteConfirmationShowing.value) {
                    flipCard()
                    true
                } else false
            }

            CardAction.SwipeRight -> {
                if (!_isEditing.value && !_isDeleteConfirmationShowing.value) {
                    cardAnimationManager.swipeRight(_currentCardPosition.value)
                    true
                } else false
            }

            CardAction.SwipeLeft -> {
                if (!_isEditing.value && !_isDeleteConfirmationShowing.value) {
                    cardAnimationManager.swipeLeft(_currentCardPosition.value)
                    true
                } else false
            }

            CardAction.Edit -> {
                if (!_isDeleteConfirmationShowing.value) {
                    _isEditing.value = true
                    val card = _currentCard.value
                    _editCardContent.value = Pair(card.front, card.back)
                    true
                } else false
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
                    // For existing cards, reset the edit content to the original values
                    _editCardContent.value =
                        Pair(_currentCard.value.front, _currentCard.value.back)
                }
                true
            }

            CardAction.Delete -> {
                if (!_isEditing.value && !_isDeleteConfirmationShowing.value) {
                    showDeleteConfirmation()
                    true
                } else false
            }

            CardAction.ConfirmDelete -> {
                if (_isDeleteConfirmationShowing.value) {
                    hideDeleteConfirmation()
                    kotlinx.coroutines.MainScope().launch {
                        deleteCurrentCard()
                    }
                    true
                } else false
            }

            CardAction.CancelDelete -> {
                if (_isDeleteConfirmationShowing.value) {
                    hideDeleteConfirmation()
                    true
                } else false
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
     * Creates a new temporary card in the UI (not in the database) and switches to edit mode
     */
    override suspend fun createNewCard(deckId: String) {
        logger.d { "Creating new card for deck ID: $deckId" }
        val originalDeckId = _currentDeck.value?.id
        val wasAlreadySelectedDeck = originalDeckId == deckId

        // Store the original deck for reference
        val originalDeck = _currentDeck.value
        logger.d { "Original deck: ${originalDeck?.name} (ID: ${originalDeckId})" }

        // If we're creating a card for a different deck, save the original deck ID
        // so we can remember where we came from
        if (!wasAlreadySelectedDeck) {
            logger.d { "Switching decks from $originalDeckId to $deckId for card creation" }

            // Select the new deck to create a card in
            selectDeck(deckId)
        }

        // Create a temporary card in memory (not in the repository)
        // We'll create it in the repository only when the user saves it
        val tempCard = Card(
            id = "",  // Empty ID means it's not yet in the database
            deckId = deckId,
            front = "",
            back = "",
            position = _currentDeckCards.value.size  // Will be the last card
        )

        logger.d { "Created temporary card for deck $deckId" }

        // Set new card state
        _isNewCard.value = true

        // Set the temporary card as the current card
        _currentCard.value = tempCard

        // Reset edit fields and enter edit mode
        _editCardContent.value = Pair("", "")
        _isEditing.value = true
    }

    /**
     * Cancels a new card, discarding any changes
     */
    override suspend fun cancelNewCard() {
        if (_isNewCard.value) {
            val tempCard = _currentCard.value
            val tempDeckId = tempCard.deckId

            logger.d { "Cancelling new card with deck ID: $tempDeckId" }

            // Since we now delay card creation until save, we should never have an ID here
            // But just in case, let's check and log a warning
            if (tempCard.id.isNotEmpty()) {
                logger.w { "Temporary card unexpectedly has ID ${tempCard.id}. This should never happen." }
                // We will NOT delete it from the repository to be safe
            }

            // Reset UI state
            _isNewCard.value = false
            _isEditing.value = false
            _editCardContent.value = Pair("", "")

            // Simply reload the current deck's data
            val currentDeck = _currentDeck.value
            if (currentDeck != null) {
                logger.d { "Reloading cards for current deck ${currentDeck.id}" }

                try {
                    // Get fresh cards list from the repository
                    val freshCards = cardRepository.getCardsByDeckId(currentDeck.id)
                    logger.d { "Loaded ${freshCards.size} cards for current deck" }

                    // Update the cards list
                    _currentDeckCards.value = freshCards

                    // Update hasCards state
                    _hasCards.value = freshCards.isNotEmpty()

                    // Set position to the first card if available
                    if (freshCards.isNotEmpty()) {
                        _currentCardPosition.value = 0
                        _currentCard.value = freshCards[0]
                        logger.d { "Set current card to first card in deck: ${freshCards[0].id}" }
                    } else {
                        _currentCard.value = Card(deckId = currentDeck.id)
                        logger.d("No cards in deck, created empty card template")
                    }
                } catch (e: Exception) {
                    logger.e(e) { "Error reloading cards: ${e.message}" }
                }
            } else {
                logger.w("No current deck selected")
            }
        } else {
            logger.w("cancelNewCard called but isNewCard is false")
        }
    }

    override fun showDeleteConfirmation() {
        _isDeleteConfirmationShowing.value = true
    }

    override fun hideDeleteConfirmation() {
        _isDeleteConfirmationShowing.value = false
    }

    override suspend fun deleteCurrentCard() {
        val card = _currentCard.value
        val currentPosition = _currentCardPosition.value

        // Don't attempt to delete if card ID is empty
        if (card.id.isEmpty() || card.deckId.isEmpty()) {
            logger.w { "Attempting to delete card with empty ID or deckId" }
            return
        }

        try {
            // Delete the card from the repository
            cardRepository.deleteCard(card.id, card.deckId)

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
            updatedCards.removeAt(currentPosition)
            _currentDeckCards.value = updatedCards

            // Update hasCards state
            _hasCards.value = updatedCards.isNotEmpty()

            // Set position to the next card or first card if we're at the end
            if (updatedCards.isNotEmpty()) {
                // Keep the same position unless we were at the end of the list
                _currentCardPosition.value = currentPosition.coerceAtMost(updatedCards.size - 1)
                _currentCard.value = updatedCards[_currentCardPosition.value]
            } else {
                // No cards left, show empty card
                _currentCard.value = Card(deckId = card.deckId)
            }
        } catch (e: Exception) {
            logger.e(e) { "Error deleting card" }
            // Hide the delete confirmation dialog in case of error
            hideDeleteConfirmation()
        }
    }
}