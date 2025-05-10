package me.forketyfork.welk.presentation

sealed class CardAction {
    data object Flip : CardAction()
    data object SwipeRight : CardAction()
    data object SwipeLeft : CardAction()
    data object Edit : CardAction()
    data object SaveEdit : CardAction()
    data object CancelEdit : CardAction()
    data object NoAction : CardAction()
    data class CreateNewCard(val deckId: String) : CardAction()
}