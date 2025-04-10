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
}

class FirestoreRepository : CardRepository {
    private val firestore: FirebaseFirestore = getPlatform().initializeFirestore()
    private val cardsCollection = firestore.collection("cards")

    override suspend fun getByIndex(idx: Int): Card {
        println("Getting card with index $idx")
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
}