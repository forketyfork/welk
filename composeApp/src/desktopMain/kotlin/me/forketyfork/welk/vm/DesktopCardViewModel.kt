package me.forketyfork.welk.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import me.forketyfork.welk.domain.CardRepository
import me.forketyfork.welk.domain.DeckRepository

/**
 * The desktop implementation of the main view model.
 */
class DesktopCardViewModel(
    cardAnimationManager: CardAnimationManager,
    cardRepository: CardRepository,
    deckRepository: DeckRepository,
) : ViewModel(),
    CardViewModel by SharedCardViewModel(
        cardAnimationManager = cardAnimationManager,
        cardRepository = cardRepository,
        deckRepository = deckRepository,
    ) {
    init {
        initialize(viewModelScope)
    }
}
