package me.forketyfork.welk

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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import me.forketyfork.welk.domain.Card

@Composable
fun App(
    mainViewModel: MainViewModel = viewModel(),
    cardInteractionManager: CardInteractionManager = DefaultCardInteractionManager()
) {
    MaterialTheme {

        val currentCard = mainViewModel.currentCard.collectAsStateWithLifecycle(Card("", ""))
        val isFlipped = mainViewModel.isFlipped.collectAsStateWithLifecycle()

        val focusRequester = remember { FocusRequester() }
        LaunchedEffect(Unit) {
            focusRequester.requestFocus()
        }

        val animatedOffset by mainViewModel.cardAnimationManager.animateOffset()
        val animatedColor by mainViewModel.cardAnimationManager.animateColor()

        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .width(315.dp)
                    .height(440.dp)
                    .offset { animatedOffset }
                    .rotate(animatedOffset.x / 80.0f)
                    .border(width = 2.dp, color = Color.Black, shape = RoundedCornerShape(10.dp))
                    .background(color = animatedColor)
                    .padding(all = 20.dp)
                    .onKeyEvent { event: KeyEvent ->
                        val action = cardInteractionManager.handleKeyEvent(event)
                        mainViewModel.processAction(action)
                    }
                    .focusRequester(focusRequester)
                    .focusable(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(currentCard.value.front)
                Divider()
                if (isFlipped.value) {
                    Text(currentCard.value.back)
                }
            }
        }
    }

}
