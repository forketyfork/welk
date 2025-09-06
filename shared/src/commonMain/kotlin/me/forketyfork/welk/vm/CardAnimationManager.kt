package me.forketyfork.welk.vm

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

interface CardAnimationManager {
    val animationCompleteTrigger: StateFlow<AnimationCompleteOutcome>

    fun reset()

    fun swipeRight(idx: Int)

    fun swipeLeft(idx: Int)
}

abstract class CommonCardAnimationManager : CardAnimationManager {
    // Trigger for animation completion
    protected val _animationCompleteTrigger = MutableStateFlow(AnimationCompleteOutcome(-1, false))
    override val animationCompleteTrigger: StateFlow<AnimationCompleteOutcome> =
        _animationCompleteTrigger.asStateFlow()
}

data class AnimationCompleteOutcome(
    val idx: Int,
    val learned: Boolean,
)
