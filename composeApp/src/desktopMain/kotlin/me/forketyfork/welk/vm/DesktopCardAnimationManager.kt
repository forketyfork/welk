package me.forketyfork.welk.vm

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.MutableStateFlow
import me.forketyfork.welk.theme.AppColors
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.absoluteValue
import kotlin.math.roundToInt

class DesktopCardAnimationManager : CommonCardAnimationManager() {

    private val _cardAnimationState = MutableStateFlow(CardAnimationState())
    private val cardAnimationState = _cardAnimationState.asStateFlow()

    @Composable
    fun animateOffset() = with(cardAnimationState.collectAsState()) {
        animateDpAsState(
            targetValue = value.offset,
            animationSpec = tween(durationMillis = 1000),
            label = "offset"
        ).also { animatedOffset ->
            LaunchedEffect(animatedOffset.value) {
                if (animatedOffset.value == 1600.dp || animatedOffset.value == (-1600).dp) {
                    // TODO eww
                    _animationCompleteTrigger.value =
                        AnimationCompleteOutcome(value.idx, animatedOffset.value == 1600.dp)
                }
            }
        }.let { animatedOffset ->
            derivedStateOf {
                val x = animatedOffset.value.value
                val y = -((x * x) / 12000 - 100).absoluteValue
                IntOffset(x.roundToInt(), y.roundToInt())
            }
        }
    }

    @Composable
    fun animateColor() = with(cardAnimationState.collectAsState()) {
        animateColorAsState(
            targetValue = value.targetColor,
            animationSpec = tween(durationMillis = 1000),
            label = "color"
        )
    }

    override fun reset() {
        _cardAnimationState.value = CardAnimationState()
    }

    override fun swipeRight(idx: Int) {
        _cardAnimationState.value = CardAnimationState(
            idx = idx,
            offset = (1600).dp,
            targetColor = AppColors.animationGreenLight,
        )
    }

    override fun swipeLeft(idx: Int) {
        _cardAnimationState.value = CardAnimationState(
            idx = idx,
            offset = (-1600).dp,
            targetColor = AppColors.animationRedLight,
        )
    }
}

data class CardAnimationState(
    val idx: Int = -1,
    val offset: Dp = 0.dp,
    val targetColor: Color = AppColors.transparent
)
