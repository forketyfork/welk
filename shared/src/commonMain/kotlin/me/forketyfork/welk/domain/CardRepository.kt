package me.forketyfork.welk.domain

interface CardRepository {
    fun getByIndex(idx: Int): Card
    fun getCardCount(): Int
}

class HardcodedCardRepository : CardRepository {

    private val cards = listOf(
        Card("welk", "увядший"),
        Card("das Laken", "простынь"),
        Card("das Kissen", "подушка")
    )


    override fun getByIndex(idx: Int) = cards[idx]

    override fun getCardCount() = cards.size

}