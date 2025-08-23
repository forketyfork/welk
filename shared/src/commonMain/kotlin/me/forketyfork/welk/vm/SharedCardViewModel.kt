package me.forketyfork.welk.vm

import co.touchlab.kermit.Logger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlin.time.Instant
import me.forketyfork.welk.domain.Card
import me.forketyfork.welk.domain.CardRepository
import me.forketyfork.welk.domain.CardReview
import me.forketyfork.welk.domain.Deck
import me.forketyfork.welk.domain.DeckRepository
import me.forketyfork.welk.domain.ReviewGrade
import me.forketyfork.welk.presentation.CardAction

open class SharedCardViewModel(
    private val cardAnimationManager: CardAnimationManager,
    private val cardRepository: CardRepository,
    private val deckRepository: DeckRepository,
) : CardViewModel, BaseInitializableViewModel() {

    companion object {
        private val logger = Logger.withTag("CommonCardViewModel")
    }

    /** Scope that is active only while the user is logged in. */
    private var sessionJob: Job? = null
    private var sessionScope: CoroutineScope? = null

    private val activeScope: CoroutineScope
        get() = sessionScope ?: viewModelScope

    // Current position within the deck
    private val _currentCardPosition = MutableStateFlow(0)

    // Current selected deck
    private val _currentDeck = MutableStateFlow<StateFlow<Deck>?>(null)
    override val currentDeck: StateFlow<StateFlow<Deck>?> = _currentDeck.asStateFlow()

    // List of available decks
    override val availableDecks: MutableStateFlow<List<StateFlow<Deck>>> =
        MutableStateFlow(emptyList())

    // Set of expanded deck IDs
    private val _expandedDeckIds = MutableStateFlow<Set<String>>(emptySet())
    override val expandedDeckIds: StateFlow<Set<String>> = _expandedDeckIds.asStateFlow()

    // List of cards in the current deck (cached to avoid repeated Firestore queries)
    private val _currentDeckCards = MutableStateFlow<List<Card>>(emptyList())
    
    // Show all cards or only due cards
    private val _showAllCards = MutableStateFlow(false)
    override val showAllCards: StateFlow<Boolean> = _showAllCards.asStateFlow()
    
    // Filtered cards based on review status and showAllCards setting
    override val currentDeckCards: StateFlow<List<Card>> by lazy {
        combine(_currentDeckCards, _showAllCards) { allCards, showAll ->
            if (showAll) {
                allCards
            } else {
                val now = Instant.fromEpochMilliseconds(System.currentTimeMillis())
                allCards.filter { card ->
                    card.nextReview == null || card.nextReview <= now
                }
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyList())
    }

    override val reviewedCardCount: StateFlow<Int> by lazy {
        _currentDeckCards
            .map { cards -> cards.count { it.reviews.isNotEmpty() } }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), 0)
    }

    override val dueCardCount: StateFlow<Int> by lazy {
        _currentDeckCards
            .map { cards -> 
                val now = Instant.fromEpochMilliseconds(System.currentTimeMillis())
                cards.count { card -> 
                    card.nextReview == null || card.nextReview <= now
                }
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), 0)
    }

    override val totalCardCount: StateFlow<Int> by lazy {
        _currentDeckCards
            .map { cards -> cards.size }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), 0)
    }

    private val _isFlipped = MutableStateFlow(false)
    override val isFlipped: StateFlow<Boolean> = _isFlipped.asStateFlow()

    private val _isEditing = MutableStateFlow(false)
    override val isEditing: StateFlow<Boolean> = _isEditing.asStateFlow()

    private val _editCardContent = MutableStateFlow(Pair("", ""))
    override val editCardContent: StateFlow<Pair<String, String>> = _editCardContent.asStateFlow()

    private val _currentCard = MutableStateFlow<Card?>(null)
    override val currentCard: StateFlow<Card?> = _currentCard.asStateFlow()

    // TODO maybe just look at the card id?
    private val _isNewCard = MutableStateFlow(false)
    override val isNewCard: StateFlow<Boolean> = _isNewCard.asStateFlow()

    private val _isDeleteConfirmationShowing = MutableStateFlow(false)
    override val isDeleteConfirmationShowing: StateFlow<Boolean> =
        _isDeleteConfirmationShowing.asStateFlow()

    /**
     * Starts collecting of card position change events, e.g., when we select a new deck,
     * and the position resets to 0. Updates the UI accordingly.
     */
    private suspend fun collectCurrentCardPositionChanges() {
        _currentCardPosition.collect { position ->
            updateCurrentCardFromPosition()
        }
    }

    /**
     * Starts the collection of current deck changes, e.g., when the user clicks on a different
     * deck, and we need to switch the card view.
     */
    private suspend fun collectCurrentDeckChanges() {
        // When the deck changes, load its cards
        _currentDeck.collect { deck ->
            logger.d { "Current deck changed to ${deck?.value?.name}" }
            if (deck != null) {
                val deckId = deck.value.id ?: error("Deck ID is null for a persistent deck")
                _currentDeckCards.value = getAllCardsForDeck(deckId)
                // Reset to the first card in the deck
                _currentCardPosition.value = 0
                updateCurrentCardFromPosition()
            }
        }
    }

    /**
     * Fetches the current card based on position in the current deck
     */
    private fun updateCurrentCardFromPosition() {
        val cards = _currentDeckCards.value

        if (cards.isEmpty()) {
            _currentCard.value = null
            return
        }

        val position = _currentCardPosition.value.coerceIn(0, cards.size - 1)
        _currentCard.value = cards.getOrNull(position)
    }

    /**
     * Recursively collects all cards for the given deck and its child decks.
     */
    private suspend fun getAllCardsForDeck(deckId: String): List<Card> {
        val cards = mutableListOf<Card>()

        suspend fun gather(id: String) {
            cards += cardRepository.getCardsByDeckId(id)
            val children = deckRepository.getChildDecks(id)
            children.forEach { child ->
                child.id?.let { gather(it) }
            }
        }

        gather(deckId)
        return cards
    }

    /**
     * Loads all available decks
     */
    suspend fun collectDeckListChanges() {
        deckRepository.flowDeckFlows().collect { decks ->
            availableDecks.value = decks.map { it.stateIn(activeScope) }
            // If we don't have a selected deck yet but decks exist, select the first one
            if (_currentDeck.value == null && availableDecks.value.isNotEmpty()) {
                val deck = availableDecks.value.first().value
                val deckId = deck.id ?: error("Deck ID is null for a persistent deck")
                selectDeck(deckId)
            }
        }
    }

    /**
     * Selects a deck by its ID and loads its cards
     */
    override suspend fun selectDeck(deckId: String) {
        try {
            logger.d { "Selecting deck with ID: $deckId" }
            val deck = deckRepository.flowDeck(deckId).stateIn(activeScope)
            _currentDeck.value = deck

            // Load cards for this deck
            val cards = getAllCardsForDeck(deckId)
            logger.d { "Loaded ${cards.size} cards for deck $deckId" }

            // Store the cards in our local cache
            _currentDeckCards.value = cards

            // Reset position and flip state
            _currentCardPosition.value = 0
            _isFlipped.value = false

            logger.i { "Deck selected: ${deck.value.name}, card count: ${cards.size}" }
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

    override suspend fun gradeCard(grade: ReviewGrade) {
        val currentCard = _currentCard.value
        val currentCardId = currentCard?.id ?: return
        
        if (currentCardId.isEmpty()) {
            logger.w { "Cannot grade card with empty ID" }
            return
        }

        try {
            // Add review to the current card
            val nextReviewTime = cardRepository.addCardReview(
                currentCardId,
                currentCard.deckId,
                grade
            )

            // Update the local card list with the new review data
            val idx = _currentCardPosition.value
            val updatedCards = _currentDeckCards.value.toMutableList()
            if (idx in updatedCards.indices) {
                val currentTime = Instant.fromEpochMilliseconds(System.currentTimeMillis())
                val newReview = CardReview(currentTime, grade)
                val updated = updatedCards[idx].copy(
                    reviews = updatedCards[idx].reviews + newReview,
                    nextReview = nextReviewTime
                )
                updatedCards[idx] = updated
                _currentDeckCards.value = updatedCards
            }

            // Move to the next card
            nextCard()
        } catch (e: Exception) {
            logger.e(e) { "Error grading card: ${e.message}" }
        }
    }

    /**
     * Starts the collection of the card animation completion events when the user swipes the card
     * to the left or to the right.
     * When the animation is completed, we add a review to the card and update the UI accordingly.
     */
    private suspend fun collectCardAnimationCompletion() {
        cardAnimationManager.animationCompleteTrigger
            .filter { it.idx != -1 } // skipping initial value
            .collect {
                val currentCard = _currentCard.value
                val currentCardId = currentCard?.id ?: error("Card ID is null")
                
                // Convert learned status to grade (backward compatibility)
                val grade = if (it.learned) ReviewGrade.GOOD else ReviewGrade.AGAIN
                
                // Add review to the current card
                val nextReviewTime = cardRepository.addCardReview(
                    currentCardId,
                    currentCard.deckId,
                    grade
                )

                // Update the local card list with the new review data
                val idx = _currentCardPosition.value
                val updatedCards = _currentDeckCards.value.toMutableList()
                if (idx in updatedCards.indices) {
                    val currentTime = Instant.fromEpochMilliseconds(System.currentTimeMillis())
                    val newReview = me.forketyfork.welk.domain.CardReview(currentTime, grade)
                    val updated = updatedCards[idx].copy(
                        reviews = updatedCards[idx].reviews + newReview,
                        nextReview = nextReviewTime
                    )
                    updatedCards[idx] = updated
                    _currentDeckCards.value = updatedCards
                }

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
                    // If this is a new card with empty content, cancel it
                    cancelNewCard()
                }
                return
            }

            // Check if we're saving a new card or updating an existing one
            if (_isNewCard.value || currentCard?.id == null) {
                logger.i("Creating a new card in the database")

                // Get the deck ID
                val deckId = currentCard?.deckId ?: _currentDeck.value?.value?.id ?: error("No deck ID")

                // Create a new card in the repository with the edited content
                val newCard = cardRepository.createCard(
                    deckId = deckId,
                    front = front,
                    back = back
                )

                // Update the view model with the new card
                _currentCard.value = newCard

                // Add the new card to the card list
                val updatedCards = _currentDeckCards.value.toMutableList()
                updatedCards.add(newCard)
                _currentDeckCards.value = updatedCards
                _currentCardPosition.value = updatedCards.size - 1

                // Mark as no longer a new card since it's been saved
                _isNewCard.value = false

                return
            }

            val cardId = currentCard.id ?: error("Card ID is null for a persistent card")
            // Normal update for an existing card with a valid ID
            cardRepository.updateCardContent(cardId, currentCard.deckId, front, back)

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
                    val card = _currentCard.value ?: error("No current card")
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
                    viewModelScope.launch {
                        cancelNewCard()
                    }
                } else {
                    // For existing cards, reset the edit content to the original values
                    _editCardContent.value =
                        Pair(_currentCard.value?.front ?: "", _currentCard.value?.back ?: "")
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
                    viewModelScope.launch {
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
                viewModelScope.launch {
                    createNewCard(action.deckId)
                }
                true
            }

            is CardAction.CreateNewCardInCurrentDeck -> {
                currentDeck.value?.let { deck ->
                    val deckId = deck.value.id ?: error("Deck ID is null for a persistent deck")
                    viewModelScope.launch {
                        createNewCard(deckId)
                    }
                } ?: logger.w { "No current deck selected" }
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
        val originalDeckId = _currentDeck.value?.value?.id
        val wasAlreadySelectedDeck = originalDeckId == deckId

        // Store the original deck for reference
        val originalDeck = _currentDeck.value?.value
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
            reviews = emptyList(),
            nextReview = null,
            position = _currentDeckCards.value.size  // Will be the last card
        )

        logger.d { "Created temporary card for deck $deckId" }

        // Set the new card state
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
            val tempCard = _currentCard.value ?: error("No current card")
            val tempDeckId = tempCard.deckId

            logger.d { "Cancelling new card with deck ID: $tempDeckId" }

            // Reset UI state
            _isNewCard.value = false
            _isEditing.value = false
            _editCardContent.value = Pair("", "")

            // Reload the current deck's data
            val currentDeck = _currentDeck.value?.value
            if (currentDeck != null) {
                val deckId = currentDeck.id ?: error("Deck ID is null for a persistent deck")
                logger.d { "Reloading cards for current deck $deckId" }

                try {
                    // Get the fresh cards list from the repository
                    val freshCards = getAllCardsForDeck(deckId)
                    logger.d { "Loaded ${freshCards.size} cards for current deck" }

                    // Update the list of cards
                    _currentDeckCards.value = freshCards

                    // Set position to the first card if available
                    if (freshCards.isNotEmpty()) {
                        _currentCardPosition.value = 0
                        _currentCard.value = freshCards[0]
                        logger.d { "Set current card to first card in deck: ${freshCards[0].id}" }
                    } else {
                        _currentCard.value = null
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

    /**
     * Installs the collectors for various UI state changes in the proper coroutine scope.
     * Call this during the model initialization.
     */
    private fun installCollectors(coroutineScope: CoroutineScope) {
        logger.d { "Installing card view model collectors" }
        sessionJob?.cancel()
        sessionScope = coroutineScope
        sessionJob = coroutineScope.launch {
            launch { collectCardAnimationCompletion() }
            launch { collectCurrentCardPositionChanges() }
            launch { collectCurrentDeckChanges() }
            launch { collectDeckListChanges() }
        }
    }

    override fun initialize(viewModelScope: CoroutineScope) {
        super.initialize(viewModelScope)
        startSession()
    }

    /** Starts collecting flows for the current login session. */
    override fun startSession() {
        if (sessionJob == null) {
            val scope = CoroutineScope(viewModelScope.coroutineContext + SupervisorJob())
            installCollectors(scope)
        }
    }

    /** Stops all active collectors and resets view model state. */
    override fun stopSession() {
        // Cancel the session job and scope to stop all Firestore listeners
        sessionJob?.cancel()
        sessionJob = null
        
        // Cancel the session scope which will cancel all stateIn() flows created with activeScope
        sessionScope?.cancel()
        sessionScope = null

        _currentDeck.value = null
        availableDecks.value = emptyList()
        _currentDeckCards.value = emptyList()
        _currentCard.value = null
        _currentCardPosition.value = 0
        _isFlipped.value = false
        _isEditing.value = false
        _editCardContent.value = "" to ""
        _isNewCard.value = false
        _isDeleteConfirmationShowing.value = false
        _expandedDeckIds.value = emptySet()
        _showAllCards.value = false
    }

    override suspend fun deleteCurrentCard() {
        val card = _currentCard.value ?: error("No current card")
        val cardId = card.id ?: error("Card ID is null for a persistent card")

        check(card.deckId.isNotEmpty()) { "Card deckId should not be empty when deleting a card" }

        val currentPosition = _currentCardPosition.value

        try {
            // Delete the card from the repository
            cardRepository.deleteCard(cardId, card.deckId)

            // Remove from the local card list
            val updatedCards = _currentDeckCards.value.toMutableList()
            updatedCards.removeAt(currentPosition)
            _currentDeckCards.value = updatedCards

            // Set position to the next card or first card if we're at the end
            if (updatedCards.isNotEmpty()) {
                // Keep the same position unless we were at the end of the list
                _currentCardPosition.value = currentPosition.coerceAtMost(updatedCards.size - 1)
                _currentCard.value = updatedCards[_currentCardPosition.value]
            } else {
                // No cards left
                _currentCard.value = null
            }
        } catch (e: Exception) {
            logger.e(e) { "Error deleting card" }
            // Hide the delete confirmation dialog in case of error
            hideDeleteConfirmation()
        }
    }

    override suspend fun createDeck(name: String, description: String, parentId: String?) {
        try {
            deckRepository.createDeck(name, description, parentId)

            // If this deck has a parent, expand the parent deck to make the new child visible
            if (parentId != null) {
                expandParentDeck(parentId)
            }
        } catch (e: Exception) {
            logger.e(e) { "Error creating deck" }
        }
    }

    /**
     * Expands a deck and all its parent decks to make nested content visible
     */
    private fun expandParentDeck(deckId: String) {
        try {
            // Add the deck ID to the expanded set
            val currentExpanded = _expandedDeckIds.value.toMutableSet()
            currentExpanded.add(deckId)
            _expandedDeckIds.value = currentExpanded

            // Find the deck in our available decks list to get parent info
            val deckFlow = availableDecks.value.find { it.value.id == deckId }
            if (deckFlow != null) {
                logger.d { "Expanded deck: ${deckFlow.value.name}" }

                // If this deck has a parent, recursively expand it too
                val parentId = deckFlow.value.parentId
                if (parentId != null) {
                    expandParentDeck(parentId)
                }
            }
        } catch (e: Exception) {
            logger.w(e) { "Error expanding parent deck $deckId: ${e.message}" }
        }
    }

    /**
     * Toggles the expansion state of a deck
     */
    override fun toggleDeckExpansion(deckId: String) {
        val currentExpanded = _expandedDeckIds.value.toMutableSet()
        if (currentExpanded.contains(deckId)) {
            currentExpanded.remove(deckId)
        } else {
            currentExpanded.add(deckId)
        }
        _expandedDeckIds.value = currentExpanded
    }

    /**
     * Checks if a deck is expanded
     */
    override fun isDeckExpanded(deckId: String): Boolean {
        return _expandedDeckIds.value.contains(deckId)
    }

    override fun toggleShowAllCards() {
        _showAllCards.value = !_showAllCards.value
    }

    override suspend fun deleteDeck(deckId: String) {
        try {
            deckRepository.deleteDeck(deckId)

            if (_currentDeck.value?.value?.id == deckId) {
                // If we're deleting the current deck, switch to the first available deck
                _currentDeck.value = availableDecks.value.firstOrNull()
            }
        } catch (e: Exception) {
            logger.e(e) { "Error deleting deck" }
        }
    }
}
