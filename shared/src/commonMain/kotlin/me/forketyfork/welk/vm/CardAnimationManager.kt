package me.forketyfork.welk.vm

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

interface CardAnimationManager {
    val animationCompleteTrigger: StateFlow<Boolean>
    fun reset()
    fun swipeRight()
    fun swipeLeft()
}

abstract class CommonCardAnimationManager : CardAnimationManager {
    // Trigger for animation completion
    protected val _animationCompleteTrigger = MutableStateFlow(false)
    override val animationCompleteTrigger: StateFlow<Boolean> =
        _animationCompleteTrigger.asStateFlow()

}