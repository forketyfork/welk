package me.forketyfork.welk.domain

import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for deck-related operations.
 */
interface DeckRepository {
    /**
     * Gets all available decks.
     */
    suspend fun getAllDecks(): List<Flow<Deck>>

    /**
     * Gets a specific deck by its ID.
     */
    suspend fun getDeckById(deckId: String): Deck

    /**
     * Creates a new deck.
     */
    suspend fun createDeck(name: String, description: String): Deck

    /**
     * Updates an existing deck.
     */
    suspend fun updateDeck(deck: Deck): Deck

    /**
     * Deletes a deck and all its cards.
     */
    suspend fun deleteDeck(deckId: String)

    /**
     * Get the flow of changes for a specific deck.
     */
    fun flowDeck(deckId: String): Flow<Deck>
}