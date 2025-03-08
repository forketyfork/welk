package me.forketyfork.welk.domain

import dev.gitlive.firebase.firestore.FirebaseFirestore
import me.forketyfork.welk.getPlatform

interface CardRepository {
    suspend fun getByIndex(idx: Int): Card
    suspend fun getCardCount(): Int
}

class HardcodedCardRepository : CardRepository {

    private val cards = listOf(
        Card("welk", "увядший"),
        Card("das Laken", "простынь"),
        Card("das Kissen", "подушка")
    )


    override suspend fun getByIndex(idx: Int) = cards[idx]

    override suspend fun getCardCount() = cards.size

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

}