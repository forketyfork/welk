package me.forketyfork.welk

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import me.forketyfork.welk.domain.FirestoreRepository
import me.forketyfork.welk.vm.CardViewModel
import me.forketyfork.welk.vm.SharedCardViewModel

/**
 * The desktop implementation of the main view model.
 */
class DesktopCardViewModel(
    val cardAnimationManager: AndroidCardAnimationManager = AndroidCardAnimationManager(),
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