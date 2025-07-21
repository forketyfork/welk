package me.forketyfork.welk.fake

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import me.forketyfork.welk.domain.Card
import me.forketyfork.welk.domain.CardRepository
import me.forketyfork.welk.domain.Deck
import me.forketyfork.welk.domain.DeckRepository
import java.util.UUID

class FakeRepository : CardRepository, DeckRepository {
    private val deckFlows = mutableMapOf<String, MutableStateFlow<Deck>>()
    private val decks = MutableStateFlow<List<MutableStateFlow<Deck>>>(emptyList())
    private val cards = mutableMapOf<String, MutableMap<String, Card>>()

    override suspend fun flowDeckFlows(): Flow<List<Flow<Deck>>> =
        decks.asStateFlow()

    override fun flowDeck(deckId: String): Flow<Deck> {
        val flow = deckFlows[deckId] ?: MutableStateFlow(Deck(id = deckId))
        return flow.asStateFlow()
    }

    override suspend fun createDeck(name: String, description: String, parentId: String?) {
        val id = UUID.randomUUID().toString()
        val deck = Deck(
            id = id,
            name = name,
            description = description,
            cardCount = 0,
            lastModified = System.currentTimeMillis(),
            created = System.currentTimeMillis(),
            parentId = parentId
        )
        val flow = MutableStateFlow(deck)
        deckFlows[id] = flow
        decks.update { it + flow }
        cards[id] = mutableMapOf()
    }

    override suspend fun deleteDeck(deckId: String) {
        // delete child decks first
        val childIds = deckFlows.values.map { it.value }.filter { it.parentId == deckId }.mapNotNull { it.id }
        childIds.forEach { deleteDeck(it) }
        deckFlows.remove(deckId)?.let { flow ->
            decks.update { it - flow }
        }
        cards.remove(deckId)
    }

    override suspend fun getChildDecks(parentId: String?): List<Deck> =
        deckFlows.values.map { it.value }.filter { it.parentId == parentId }

    override fun flowChildDecks(parentId: String?): Flow<List<Deck>> =
        decks.map { flows -> flows.map { it.value }.filter { it.parentId == parentId } }

    override suspend fun getCardsByDeckId(deckId: String): List<Card> =
        cards[deckId]?.values?.sortedBy { it.position }?.toList() ?: emptyList()

    override suspend fun createCard(deckId: String, front: String, back: String): Card {
        val deckCards = cards.getOrPut(deckId) { mutableMapOf() }
        val id = UUID.randomUUID().toString()
        val card = Card(id = id, deckId = deckId, front = front, back = back, position = deckCards.size)
        deckCards[id] = card
        deckFlows[deckId]?.update { it.copy(cardCount = deckCards.size) }
        return card
    }

    override suspend fun updateCardLearnedStatus(cardId: String, deckId: String, learned: Boolean) {
        cards[deckId]?.get(cardId)?.let { card ->
            cards[deckId]?.set(cardId, card.copy(learned = learned))
        }
    }

    override suspend fun updateCardContent(cardId: String, deckId: String, front: String, back: String) {
        cards[deckId]?.get(cardId)?.let { card ->
            cards[deckId]?.set(cardId, card.copy(front = front, back = back))
        }
    }

    override suspend fun deleteCard(cardId: String, deckId: String) {
        cards[deckId]?.let { deckCards ->
            if (deckCards.remove(cardId) != null) {
                deckFlows[deckId]?.update { it.copy(cardCount = deckCards.size) }
            }
        }
    }
}
