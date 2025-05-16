package me.forketyfork.welk.vm

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow
import me.forketyfork.welk.domain.Card
import me.forketyfork.welk.domain.Deck
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
    fun updateEditContent(front: String, back: String)
    suspend fun saveCardEdit()
    suspend fun createNewCard(deckId: String)
    suspend fun cancelNewCard()
    suspend fun deleteCurrentCard()
    fun showDeleteConfirmation()
    fun hideDeleteConfirmation()

    /**
     * Installs the collectors for various UI state changes in the proper coroutine scope.
     * Call this during the model initialization.
     */
    fun installCollectors(coroutineScope: CoroutineScope)
}