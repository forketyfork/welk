package me.forketyfork.welk.domain

/**
 * Repository interface for deck-related operations.
 */
interface DeckRepository {
    /**
     * Gets all available decks.
     */
    suspend fun getAllDecks(): List<Deck>

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
}