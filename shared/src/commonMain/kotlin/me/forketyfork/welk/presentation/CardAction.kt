package me.forketyfork.welk.presentation

import me.forketyfork.welk.domain.ReviewGrade

sealed class CardAction {
    data object Flip : CardAction()

    data object SwipeRight : CardAction()

    data object SwipeLeft : CardAction()

    data object Edit : CardAction()

    data object SaveEdit : CardAction()

    data object CancelEdit : CardAction()

    data object Delete : CardAction()

    data object ConfirmDelete : CardAction()

    data object CancelDelete : CardAction()

    data object NoAction : CardAction()

    data class CreateNewCard(
        val deckId: String,
    ) : CardAction()

    data object CreateNewCardInCurrentDeck : CardAction()

    data class GradeCard(
        val grade: ReviewGrade,
    ) : CardAction()
}
