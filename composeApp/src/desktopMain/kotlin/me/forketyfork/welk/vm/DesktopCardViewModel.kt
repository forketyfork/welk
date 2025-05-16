package me.forketyfork.welk.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import me.forketyfork.welk.domain.FirestoreRepository

/**
 * The desktop implementation of the main view model.
 */
class DesktopCardViewModel(
    val cardAnimationManager: DesktopCardAnimationManager = DesktopCardAnimationManager(),
    private val firestoreRepository: FirestoreRepository = FirestoreRepository(),
) : ViewModel(), CardViewModel by SharedCardViewModel(
    cardRepository = firestoreRepository,
    deckRepository = firestoreRepository,
    cardAnimationManager = cardAnimationManager
) {

    init {
        installCollectors(viewModelScope)
        viewModelScope.launch {
            // initially load the available decks
            loadDecks()
        }
    }
}