package me.forketyfork.welk

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.absoluteValue
import kotlin.math.roundToInt

class CardAnimationManager {

    private val _cardAnimationState = MutableStateFlow(CardAnimationState())
    val cardAnimationState = _cardAnimationState.asStateFlow()

    // Trigger for animation completion
    private val _animationCompleteTrigger = MutableStateFlow(false)
    val animationCompleteTrigger: StateFlow<Boolean> = _animationCompleteTrigger.asStateFlow()

    @Composable
    fun animateOffset() = animateDpAsState(
        targetValue = _cardAnimationState.value.offset,
        animationSpec = tween(durationMillis = 1000),
        label = "offset"
    ).also { animatedOffset ->
        LaunchedEffect(animatedOffset.value) {
            if (animatedOffset.value == 1600.dp || animatedOffset.value == (-1600).dp) {
                _animationCompleteTrigger.value = true
            }
        }
    }.let { animatedOffset ->
        derivedStateOf {
            val x = animatedOffset.value.value
            val y = -((x * x) / 12000 - 100).absoluteValue
            IntOffset(x.roundToInt(), y.roundToInt())
        }
    }

    @Composable
    fun animateColor() = animateColorAsState(
        targetValue = _cardAnimationState.value.targetColor,
        animationSpec = tween(durationMillis = 1000),
        label = "color"
    ).also { animatedColor ->
        LaunchedEffect(animatedColor.value) {
            if (animatedColor.value == Color.Green || animatedColor.value == Color.Red) {
                _animationCompleteTrigger.value = true
            }
        }
    }

    fun reset() {
        _cardAnimationState.value = CardAnimationState()
    }

    fun swipeRight() {
        _animationCompleteTrigger.value = false
        _cardAnimationState.value = CardAnimationState(
            offset = (1600).dp,
            targetColor = Color.Green,
        )
    }

    fun swipeLeft() {
        _animationCompleteTrigger.value = false
        _cardAnimationState.value = CardAnimationState(
            offset = (-1600).dp,
            targetColor = Color.Red,
        )
    }
}

data class CardAnimationState(
    val offset: Dp = 0.dp,
    val targetColor: Color = Color.Transparent
)
