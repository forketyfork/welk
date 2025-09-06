package me.forketyfork.welk.domain

interface CardRepository {
    /**
     * Gets all cards for a specific deck.
     */
    suspend fun getCardsByDeckId(deckId: String): List<Card>

    /**
     * Creates a new card in the specified deck.
     */
    suspend fun createCard(
        deckId: String,
        front: String,
        back: String,
    ): Card

    /**
     * Updates a card's learned status.
     */
    suspend fun updateCardLearnedStatus(
        cardId: String,
        deckId: String,
        learned: Boolean,
    )

    /**
     * Updates a card's content (front and back text).
     */
    suspend fun updateCardContent(
        cardId: String,
        deckId: String,
        front: String,
        back: String,
    )

    /**
     * Deletes a card.
     */
    suspend fun deleteCard(
        cardId: String,
        deckId: String,
    )
}
