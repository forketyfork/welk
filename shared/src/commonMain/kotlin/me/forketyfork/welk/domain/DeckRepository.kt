package me.forketyfork.welk.domain

import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for deck-related operations.
 */
interface DeckRepository {
    /**
     * Get the list of flows all available decks.
     */
    suspend fun flowDeckFlows(): Flow<List<Flow<Deck>>>

    /**
     * Get the flow of changes for a specific deck.
     */
    fun flowDeck(deckId: String): Flow<Deck>

    /**
     * Creates a new deck.
     */
    suspend fun createDeck(name: String, description: String)

    /**
     * Deletes a deck and all its cards.
     */
    suspend fun deleteDeck(deckId: String)

}