package me.forketyfork.welk.domain

import co.touchlab.kermit.Logger
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.auth
import dev.gitlive.firebase.firestore.FirebaseFirestore
import kotlinx.datetime.Clock
import me.forketyfork.welk.Platform

class FirestoreRepository(val platform: Platform) : CardRepository, DeckRepository {
    private val firestore: FirebaseFirestore = platform.initializeFirestore()
    private val logger = Logger.Companion.withTag("FirestoreRepository")
    
    private val userCollection
        get() = Firebase.auth.currentUser?.uid?.let { userId ->
            firestore.collection(userId)
        } ?: throw IllegalStateException("User must be logged in to access data")
    
    private val decksCollection
        get() = userCollection.document("collections").collection("decks")

    // DECK REPOSITORY IMPLEMENTATION

    override suspend fun getAllDecks(): List<Deck> {
        // Create some sample decks if none exist yet
        val documents = decksCollection.get().documents
        if (documents.isEmpty()) {
            createSampleDecks()
            return decksCollection.get().documents.map { it.data<Deck>() }
        }
        return documents.map { it.data<Deck>() }
    }

    override suspend fun getDeckById(deckId: String): Deck {
        return decksCollection.document(deckId).get().data<Deck>()
    }

    private suspend fun createSampleDecks() {
        // Create some sample decks
        val timestamp = Clock.System.now().toEpochMilliseconds()

        val deck1 = Deck(
            id = "deck1",
            name = "Basic Vocabulary",
            description = "Essential words for beginners",
            cardCount = 3,
            lastModified = timestamp,
            created = timestamp
        )

        val deck2 = Deck(
            id = "deck2",
            name = "Grammar Rules",
            description = "Key grammar concepts",
            cardCount = 2,
            lastModified = timestamp,
            created = timestamp
        )

        val deck3 = Deck(
            id = "deck3",
            name = "Idioms",
            description = "Common expressions and idioms",
            cardCount = 2,
            lastModified = timestamp,
            created = timestamp
        )

        // Add the decks to Firestore
        decksCollection.document(deck1.id).set(deck1)
        decksCollection.document(deck2.id).set(deck2)
        decksCollection.document(deck3.id).set(deck3)

        // Add sample cards for each deck
        val cards1 = listOf(
            Card(id = "card1_1", deckId = "deck1", front = "Hello", back = "Hola", position = 0),
            Card(id = "card1_2", deckId = "deck1", front = "Goodbye", back = "Adiós", position = 1),
            Card(
                id = "card1_3",
                deckId = "deck1",
                front = "Thank you",
                back = "Gracias",
                position = 2
            )
        )

        val cards2 = listOf(
            Card(
                id = "card2_1",
                deckId = "deck2",
                front = "Present Simple",
                back = "Used for habits and routines",
                position = 0
            ),
            Card(
                id = "card2_2",
                deckId = "deck2",
                front = "Present Continuous",
                back = "Used for actions happening now",
                position = 1
            )
        )

        val cards3 = listOf(
            Card(
                id = "card3_1",
                deckId = "deck3",
                front = "Break a leg",
                back = "Good luck",
                position = 0
            ),
            Card(
                id = "card3_2",
                deckId = "deck3",
                front = "Under the weather",
                back = "Feeling sick",
                position = 1
            )
        )

        // Add cards to their respective decks
        val deck1Cards = decksCollection.document(deck1.id).collection("cards")
        cards1.forEach { card -> deck1Cards.document(card.id).set(card) }

        val deck2Cards = decksCollection.document(deck2.id).collection("cards")
        cards2.forEach { card -> deck2Cards.document(card.id).set(card) }

        val deck3Cards = decksCollection.document(deck3.id).collection("cards")
        cards3.forEach { card -> deck3Cards.document(card.id).set(card) }
    }

    override suspend fun createDeck(name: String, description: String): Deck {
        val timestamp = Clock.System.now().toEpochMilliseconds()
        val newDeck = Deck(
            id = "", // Will be populated by Firestore
            name = name,
            description = description,
            cardCount = 0,
            lastModified = timestamp,
            created = timestamp
        )

        val docRef = decksCollection.add(newDeck)
        return newDeck.copy(id = docRef.id)
    }

    override suspend fun updateDeck(deck: Deck): Deck {
        val updatedDeck = deck.copy(lastModified = Clock.System.now().toEpochMilliseconds())
        decksCollection.document(deck.id).set(updatedDeck)
        return updatedDeck
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
        return cardsCollection.get().documents.map { it.data<Card>() }
    }

    override suspend fun getCardById(deckId: String, cardId: String): Card {
        // If either ID is empty, return an empty card rather than throwing an exception
        if (cardId.isEmpty() || deckId.isEmpty()) {
            logger.w { "Empty card ID or deck ID provided to getCardById" }
            return Card(deckId = deckId)
        }

        val cardsCollection = decksCollection.document(deckId).collection("cards")
        return cardsCollection.document(cardId).get().data<Card>()
    }

    override suspend fun getCardByPosition(deckId: String, position: Int): Card {
        val cardsCollection = decksCollection.document(deckId).collection("cards")
        val cards = cardsCollection.get().documents

        // Find the card with the specified position
        val card = cards.firstOrNull { it.data<Card>().position == position }

        if (card == null) {
            // If no card found at that position, return first card
            val firstCard = cards.minByOrNull { it.data<Card>().position }
            return firstCard?.data<Card>() ?: Card() // Return empty card if none exists
        }

        return card.data<Card>()
    }

    override suspend fun getCardCount(deckId: String): Int {
        val cardsCollection = decksCollection.document(deckId).collection("cards")
        return cardsCollection.get().documents.size
    }

    override suspend fun createCard(deckId: String, front: String, back: String): Card {
        if (deckId.isEmpty()) {
            logger.e { "Error: Cannot create card with empty deckId" }
            return Card()
        }

        val cardsCollection = decksCollection.document(deckId).collection("cards")

        // Get current card count to determine position
        val cardCount = getCardCount(deckId)

        val newCard = Card(
            id = "", // Will be populated by Firestore
            deckId = deckId,
            front = front,
            back = back,
            position = cardCount // Position it at the end of the deck
        )

        // Add the card to Firestore and get the generated document reference
        val docRef = cardsCollection.add(newCard)

        // Create a copy of the card with the Firestore-generated ID
        val cardWithId = newCard.copy(id = docRef.id)

        // Update the card in Firestore with its ID to ensure it's properly stored
        cardsCollection.document(docRef.id).set(cardWithId)

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