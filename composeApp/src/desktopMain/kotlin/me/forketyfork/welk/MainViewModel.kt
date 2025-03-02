package me.forketyfork.welk

import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel

class MainViewModel : ViewModel() {

    private val cardStore = CardStore()

    private val idx = mutableStateOf(0)

    val open = mutableStateOf(false)

    val currentCard = derivedStateOf {
        cardStore.cards[idx.value]
    }

    fun flipCard() {
        open.value = !open.value
    }

    fun nextCard() {
        idx.value = idx.value.inc() % cardStore.cards.size
        open.value = false
    }

}
