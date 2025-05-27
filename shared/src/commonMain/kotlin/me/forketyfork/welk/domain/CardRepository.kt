package me.forketyfork.welk.domain

interface CardRepository {
    /**
     * Gets all cards for a specific deck.
     */
    suspend fun getCardsByDeckId(deckId: String): List<Card>

    /**
     * Gets a specific card by its ID.
     */
    suspend fun getCardById(deckId: String, cardId: String): Card

    /**
     * Gets the total number of cards in a deck.
     */
    suspend fun getCardCount(deckId: String): Int

    /**
     * Creates a new card in the specified deck.
     */
    suspend fun createCard(deckId: String, front: String, back: String): Card

    /**
     * Updates a card's learned status.
     */
    suspend fun updateCardLearnedStatus(cardId: String, deckId: String, learned: Boolean)

    /**
     * Updates a card's content (front and back text).
     */
    suspend fun updateCardContent(cardId: String, deckId: String, front: String, back: String)

    /**
     * Deletes a card.
     */
    suspend fun deleteCard(cardId: String, deckId: String)
}

