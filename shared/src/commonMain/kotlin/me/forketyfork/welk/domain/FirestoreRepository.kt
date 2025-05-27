package me.forketyfork.welk.domain

import co.touchlab.kermit.Logger
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.auth
import dev.gitlive.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.datetime.Clock
import me.forketyfork.welk.Platform

class FirestoreRepository(val platform: Platform) : CardRepository, DeckRepository {

    companion object {
        private val logger = Logger.Companion.withTag("FirestoreRepository")
    }

    private val firestore: FirebaseFirestore = platform.initializeFirestore()
    private val users = firestore.collection("users")

    private val userDocument
        get() = Firebase.auth.currentUser?.uid?.let { userId ->
            users.document(userId)
        } ?: throw IllegalStateException("User must be logged in to access data")

    private val decksCollection
        get() = userDocument.collection("decks")

    // DECK REPOSITORY IMPLEMENTATION

    override suspend fun flowDeckFlows(): Flow<List<Flow<Deck>>> {
        if (decksCollection.get().documents.isEmpty()) {
            // Create some sample decks if none exist yet
            createSampleDecks()
        }
        return decksCollection.snapshots.map { it.documents }
            .map { documentSnapshots ->
                documentSnapshots.map { documentSnapshot ->
                    documentSnapshot.reference.snapshots.mapNotNull { it ->
                        it.data<Deck?>()?.apply { id = documentSnapshot.id }
                    }
                }
            }
    }

    suspend fun getDeckById(deckId: String): Deck {
        return decksCollection.document(deckId).get().data<Deck>().apply { id = deckId }
    }

    override fun flowDeck(deckId: String): Flow<Deck> =
        decksCollection.document(deckId).snapshots.mapNotNull { it.data<Deck?>()?.apply { id = deckId } }

    private suspend fun createSampleDecks() {
        // Create some sample decks
        val timestamp = Clock.System.now().toEpochMilliseconds()

        val deck1 = Deck(
            name = "Basic Vocabulary",
            description = "Essential words for beginners",
            cardCount = 3,
            lastModified = timestamp,
            created = timestamp
        )

        val deck2 = Deck(
            name = "Grammar Rules",
            description = "Key grammar concepts",
            cardCount = 2,
            lastModified = timestamp,
            created = timestamp
        )

        val deck3 = Deck(
            name = "Idioms",
            description = "Common expressions and idioms",
            cardCount = 2,
            lastModified = timestamp,
            created = timestamp
        )

        // Add the decks to Firestore
        decksCollection.document("deck1").set(deck1)
        decksCollection.document("deck2").set(deck2)
        decksCollection.document("deck3").set(deck3)

        // Add sample cards for each deck
        val card11 = Card(deckId = "deck1", front = "Hello", back = "Hola", position = 0)
        val card12 = Card(deckId = "deck1", front = "Goodbye", back = "Adiós", position = 1)
        val card13 = Card(deckId = "deck1", front = "Thank you", back = "Gracias", position = 2)
        val card21 =
            Card(deckId = "deck2", front = "Present Simple", back = "Used for habits and routines", position = 0)
        val card22 =
            Card(deckId = "deck2", front = "Present Continuous", back = "Used for actions happening now", position = 1)
        val card31 = Card(deckId = "deck3", front = "Break a leg", back = "Good luck", position = 0)
        val card32 = Card(deckId = "deck3", front = "Under the weather", back = "Feeling sick", position = 1)

        // Add cards to their respective decks
        val deck1Cards = decksCollection.document("deck1").collection("cards")
        deck1Cards.document("card11").set(card11)
        deck1Cards.document("card12").set(card12)
        deck1Cards.document("card13").set(card13)

        val deck2Cards = decksCollection.document("deck2").collection("cards")
        deck2Cards.document("card21").set(card21)
        deck2Cards.document("card22").set(card22)

        val deck3Cards = decksCollection.document("deck3").collection("cards")
        deck3Cards.document("card31").set(card31)
        deck3Cards.document("card32").set(card32)
    }

    override suspend fun createDeck(name: String, description: String) {
        val timestamp = Clock.System.now().toEpochMilliseconds()
        val newDeck = Deck(
            name = name,
            description = description,
            cardCount = 0,
            lastModified = timestamp,
            created = timestamp
        )
        decksCollection.add(newDeck)
    }

    override suspend fun deleteDeck(deckId: String) {
        // First delete all cards in the deck
        val cardsCollection = decksCollection.document(deckId).collection("cards")
        val cards = cardsCollection.get().documents

        // Delete each card
        cards.forEach { cardDoc ->
            cardsCollection.document(cardDoc.id).delete()
        }

        // Then delete the deck itself
        decksCollection.document(deckId).delete()
    }

    // CARD REPOSITORY IMPLEMENTATION

    override suspend fun getCardsByDeckId(deckId: String): List<Card> {
        val cardsCollection = decksCollection.document(deckId).collection("cards")
        return cardsCollection.get().documents.map { it.data<Card>().apply { id = it.id } }
    }

    override suspend fun getCardById(deckId: String, cardId: String): Card {
        val cardsCollection = decksCollection.document(deckId).collection("cards")
        return cardsCollection.document(cardId).get().data<Card>().apply { id = cardId }
    }

    override suspend fun getCardCount(deckId: String): Int {
        val cardsCollection = decksCollection.document(deckId).collection("cards")
        return cardsCollection.get().documents.size
    }

    override suspend fun createCard(deckId: String, front: String, back: String): Card {
        val cardsCollection = decksCollection.document(deckId).collection("cards")

        // Get current card count to determine position
        val cardCount = getCardCount(deckId)

        val newCard = Card(
            deckId = deckId,
            front = front,
            back = back,
            position = cardCount // Position it at the end of the deck
        )

        // Add the card to Firestore and get the generated document reference
        val docRef = cardsCollection.add(newCard)

        // Create a copy of the card with the Firestore-generated ID
        val cardWithId = newCard.copy(id = docRef.id)

        // Update the deck's card count
        val deck = getDeckById(deckId)
        val updatedDeck = deck.copy(
            cardCount = cardCount + 1,
            lastModified = Clock.System.now().toEpochMilliseconds()
        )
        decksCollection.document(deckId).set(updatedDeck)

        // Return the card with the proper ID
        return cardWithId
    }

    override suspend fun updateCardLearnedStatus(cardId: String, deckId: String, learned: Boolean) {
        // Check if card ID or deck ID are invalid
        if (cardId.isEmpty() || deckId.isEmpty()) {
            logger.e { "Error: Cannot update card with empty ID or deckId" }
            return
        }

        val cardsCollection = decksCollection.document(deckId).collection("cards")
        val card = getCardById(deckId, cardId)
        val updatedCard = card.copy()
        updatedCard.learned = learned

        // Use set instead of update to ensure the operation works for all cards
        cardsCollection.document(cardId).set(updatedCard)

        // Update the deck's last modified timestamp
        val deck = getDeckById(deckId)
        val updatedDeck = deck.copy(lastModified = Clock.System.now().toEpochMilliseconds())
        decksCollection.document(deckId).set(updatedDeck)
    }

    override suspend fun updateCardContent(
        cardId: String,
        deckId: String,
        front: String,
        back: String
    ) {
        // Check if card ID or deck ID are invalid
        if (cardId.isEmpty() || deckId.isEmpty()) {
            logger.e { "Cannot update card with empty ID or deckId" }
            return
        }

        val cardsCollection = decksCollection.document(deckId).collection("cards")
        val card = getCardById(deckId, cardId)
        val updatedCard = card.copy(front = front, back = back)

        // Use set instead of update to ensure the operation works for all cards
        cardsCollection.document(cardId).set(updatedCard)

        // Update the deck's last modified timestamp
        val deck = getDeckById(deckId)
        val updatedDeck = deck.copy(lastModified = Clock.System.now().toEpochMilliseconds())
        decksCollection.document(deckId).set(updatedDeck)
    }

    override suspend fun deleteCard(cardId: String, deckId: String) {
        // Check if card ID or deck ID are invalid
        if (cardId.isEmpty() || deckId.isEmpty()) {
            logger.e { "Cannot delete card with empty ID or deckId" }
            return
        }

        val cardsCollection = decksCollection.document(deckId).collection("cards")
        cardsCollection.document(cardId).delete()

        // Update the deck's card count
        val cardCount = getCardCount(deckId)
        val deck = getDeckById(deckId)
        val updatedDeck = deck.copy(
            cardCount = cardCount,
            lastModified = Clock.System.now().toEpochMilliseconds()
        )
        decksCollection.document(deckId).set(updatedDeck)
    }
}