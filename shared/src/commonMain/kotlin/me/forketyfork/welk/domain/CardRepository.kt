package me.forketyfork.welk.domain

import kotlin.time.Instant

interface CardRepository {
    /**
     * Gets all cards for a specific deck.
     */
    suspend fun getCardsByDeckId(deckId: String): List<Card>

    /**
     * Creates a new card in the specified deck.
     */
    suspend fun createCard(deckId: String, front: String, back: String): Card

    /**
     * Adds a review to a card's history and updates the next review timestamp.
     * @param cardId ID of the card being reviewed
     * @param deckId ID of the deck containing the card
     * @param grade User's assessment of how well they knew the card
     * @return The calculated next review timestamp
     */
    suspend fun addCardReview(cardId: String, deckId: String, grade: ReviewGrade): Instant

    /**
     * Updates a card's content (front and back text).
     */
    suspend fun updateCardContent(cardId: String, deckId: String, front: String, back: String)

    /**
     * Deletes a card.
     */
    suspend fun deleteCard(cardId: String, deckId: String)
}

