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
     * @param name The name of the deck
     * @param description The description of the deck
     * @param parentId The ID of the parent deck, or null for a top-level deck
     */
    suspend fun createDeck(name: String, description: String, parentId: String? = null)

    /**
     * Deletes a deck, all its cards, and all its child decks.
     */
    suspend fun deleteDeck(deckId: String)

    /**
     * Get all child decks for a given parent deck.
     * @param parentId The ID of the parent deck, or null to get top-level decks
     */
    suspend fun getChildDecks(parentId: String?): List<Deck>

    /**
     * Get a flow of all child decks for a given parent deck.
     * @param parentId The ID of the parent deck, or null to get top-level decks
     */
    fun flowChildDecks(parentId: String?): Flow<List<Deck>>
}
