package me.forketyfork.welk

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Divider
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.flow.collectLatest
import kotlin.math.absoluteValue
import kotlin.math.roundToInt

@Composable
fun App(mainViewModel: MainViewModel = viewModel()) {
    MaterialTheme {
        val currentCard = mainViewModel.currentCard.value
        val focusRequester = remember { FocusRequester() }

        var offset by remember { mutableStateOf(0.dp) }
        var targetColor by remember { mutableStateOf(Color.Transparent) }
        var isAnimating by remember { mutableStateOf(false) }

        val animatedOffset by animateDpAsState(
            targetValue = offset,
            animationSpec = tween(durationMillis = 1000),
            label = "offset"
        )

        val animatedColor by animateColorAsState(
            targetValue = targetColor,
            animationSpec = tween(durationMillis = 1000),
            label = "color"
        )

        LaunchedEffect(Unit) {
            focusRequester.requestFocus()
        }

        LaunchedEffect(animatedOffset, animatedColor) {
            snapshotFlow { animatedOffset }.collectLatest {
                if (it == 1600.dp || it == (-1600).dp) {
                    isAnimating = false
                }
            }
            snapshotFlow { animatedColor }.collectLatest {
                if (it == Color.Green || it == Color.Red) {
                    isAnimating = false
                }
            }
        }

        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .width(315.dp)
                    .height(440.dp)
                    .offset {
                        val x = animatedOffset.value
                        val y = -((x * x) / 12000 - 100).absoluteValue
                        IntOffset(x.roundToInt(), y.roundToInt())
                    }
                    .rotate(animatedOffset.value / 80)
                    .border(width = 2.dp, color = Color.Black, shape = RoundedCornerShape(10.dp))
                    .background(color = animatedColor)
                    .padding(all = 20.dp)
                    .onKeyEvent { event: KeyEvent ->
                        if (event.type == KeyEventType.KeyUp) {
                            when (event.key) {
                                Key.Spacebar -> {
                                    mainViewModel.flipCard()
                                    return@onKeyEvent true
                                }

                                Key.DirectionRight -> {
                                    offset = (1600).dp
                                    targetColor = Color.Green
                                    isAnimating = true
                                    return@onKeyEvent true
                                }

                                Key.DirectionLeft -> {
                                    offset = (-1600).dp
                                    targetColor = Color.Red
                                    isAnimating = true
                                    return@onKeyEvent true
                                }
                            }
                        }
                        false
                    }
                    .focusRequester(focusRequester)
                    .focusable(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(currentCard.front)
                Divider()
                if (mainViewModel.open.value) {
                    Text(currentCard.back)
                }
            }
        }
        LaunchedEffect(isAnimating) {
            if (!isAnimating) {
                if (offset != 0.dp) {
                    mainViewModel.nextCard()
                }
                offset = 0.dp
                targetColor = Color.Transparent
            }
        }
    }

}
