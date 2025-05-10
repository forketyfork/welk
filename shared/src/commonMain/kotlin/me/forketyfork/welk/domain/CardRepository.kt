package me.forketyfork.welk.domain

import dev.gitlive.firebase.firestore.FirebaseFirestore
import me.forketyfork.welk.getPlatform

interface CardRepository {
    suspend fun getByIndex(idx: Int): Card
    suspend fun getCardCount(): Int

    /**
     * Called when the user swipes, marking the card as learned or forgotten.
     */
    suspend fun updateCardLearnedStatus(idx: Int, learned: Boolean)

    /**
     * Updates the card's content (front and back text).
     */
    suspend fun updateCardContent(idx: Int, front: String, back: String)
}

class FirestoreRepository : CardRepository {
    private val firestore: FirebaseFirestore = getPlatform().initializeFirestore()
    private val cardsCollection = firestore.collection("cards")

    override suspend fun getByIndex(idx: Int): Card {
        return cardsCollection.document(idx.toString()).get().data<Card>()
    }

    override suspend fun getCardCount(): Int {
        return cardsCollection.get().documents.size
    }

    override suspend fun updateCardLearnedStatus(idx: Int, learned: Boolean) {
        val card = getByIndex(idx)
        card.learned = learned
        cardsCollection.document(idx.toString())
            .update(card) {
                this.encodeDefaults = encodeDefaults
            }
    }

    override suspend fun updateCardContent(idx: Int, front: String, back: String) {
        val card = getByIndex(idx)
        val updatedCard = card.copy(front = front, back = back)
        cardsCollection.document(idx.toString())
            .update(updatedCard) {
                this.encodeDefaults = encodeDefaults
            }
    }
}