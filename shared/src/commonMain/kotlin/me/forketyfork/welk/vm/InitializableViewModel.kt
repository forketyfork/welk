package me.forketyfork.welk.vm

import kotlinx.coroutines.CoroutineScope

interface InitializableViewModel {

    fun initialize(viewModelScope: CoroutineScope)

}
