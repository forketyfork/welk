package me.forketyfork.welk

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import me.forketyfork.welk.domain.CardRepository
import me.forketyfork.welk.domain.HardcodedCardRepository
import me.forketyfork.welk.vm.CardViewModel
import me.forketyfork.welk.vm.CommonCardViewModel

class MainViewModel(
    val cardAnimationManager: AndroidCardAnimationManager = AndroidCardAnimationManager(),
    private val cardRepository: CardRepository = HardcodedCardRepository()
) : ViewModel(), CardViewModel by CommonCardViewModel(cardRepository, cardAnimationManager) {

    init {
        viewModelScope.launch { nextCardOnAnimationCompletion() }
    }
}
