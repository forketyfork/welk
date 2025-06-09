package me.forketyfork.welk.vm

import kotlinx.coroutines.flow.StateFlow
import me.forketyfork.welk.domain.Card
import me.forketyfork.welk.domain.Deck
import me.forketyfork.welk.presentation.CardAction

interface CardViewModel : InitializableViewModel {
    val isFlipped: StateFlow<Boolean>
    val isEditing: StateFlow<Boolean>
    val currentCard: StateFlow<Card?>
    val editCardContent: StateFlow<Pair<String, String>>
    val currentDeck: StateFlow<StateFlow<Deck>?>
    val availableDecks: StateFlow<List<StateFlow<Deck>>>
    val isNewCard: StateFlow<Boolean>
    val isDeleteConfirmationShowing: StateFlow<Boolean>
    val learnedCardCount: StateFlow<Int>
    val expandedDeckIds: StateFlow<Set<String>>

    fun flipCard()
    suspend fun nextCard()
    suspend fun selectDeck(deckId: String)
    fun processAction(action: CardAction): Boolean
    fun updateEditContent(front: String, back: String)
    suspend fun saveCardEdit()
    suspend fun createNewCard(deckId: String)
    suspend fun cancelNewCard()
    suspend fun createDeck(name: String, description: String, parentId: String? = null)
    suspend fun deleteDeck(deckId: String)
    suspend fun deleteCurrentCard()
    fun showDeleteConfirmation()
    fun hideDeleteConfirmation()
    fun toggleDeckExpansion(deckId: String)
    fun isDeckExpanded(deckId: String): Boolean
}