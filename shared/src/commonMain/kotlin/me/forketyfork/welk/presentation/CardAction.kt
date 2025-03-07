package me.forketyfork.welk.presentation

sealed class CardAction {
    data object Flip : CardAction()
    data object SwipeRight : CardAction()
    data object SwipeLeft : CardAction()
    data object NoAction : CardAction()
}