package me.forketyfork.welk.vm

import kotlinx.coroutines.CoroutineScope

open class BaseInitializableViewModel : InitializableViewModel {
    lateinit var viewModelScope: CoroutineScope

    override fun initialize(viewModelScope: CoroutineScope) {
        this.viewModelScope = viewModelScope
    }
}
