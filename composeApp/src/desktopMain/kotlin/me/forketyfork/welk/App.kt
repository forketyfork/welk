package me.forketyfork.welk

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
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
import kotlinx.coroutines.launch
import me.forketyfork.welk.presentation.CardAction

@Composable
fun App(
    mainViewModel: MainViewModel = viewModel(),
    cardInteractionManager: CardInteractionManager = DefaultCardInteractionManager()
) {
    MaterialTheme {

        val currentCard = mainViewModel.currentCard.collectAsStateWithLifecycle()
        val isFlipped = mainViewModel.isFlipped.collectAsStateWithLifecycle()
        val isEditing = mainViewModel.isEditing.collectAsStateWithLifecycle()
        val editCardContent = mainViewModel.editCardContent.collectAsStateWithLifecycle()

        var frontText by remember { mutableStateOf("") }
        var backText by remember { mutableStateOf("") }

        val coroutineScope = rememberCoroutineScope()

        // Update local state when edit mode is activated
        LaunchedEffect(editCardContent.value) {
            val (front, back) = editCardContent.value
            frontText = front
            backText = back
        }

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
                if (isEditing.value) {
                    // Edit mode UI
                    OutlinedTextField(
                        value = frontText,
                        onValueChange = { frontText = it },
                        label = { Text("Front") },
                        modifier = Modifier.fillMaxWidth().weight(1f)
                    )
                    Divider()
                    OutlinedTextField(
                        value = backText,
                        onValueChange = { backText = it },
                        label = { Text("Back") },
                        modifier = Modifier.fillMaxWidth().weight(1f)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        Button(
                            onClick = {
                                mainViewModel.updateEditContent(frontText, backText)
                                coroutineScope.launch {
                                    // First save the edits to update the card content
                                    mainViewModel.saveCardEdit()
                                    // Then exit edit mode
                                    mainViewModel.processAction(CardAction.SaveEdit)
                                    // Return focus to the card for keyboard navigation
                                    focusRequester.requestFocus()
                                }
                            }
                        ) {
                            Text("Save")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                // Cancel without saving changes and reset to original values
                                mainViewModel.processAction(CardAction.CancelEdit)
                                // Return focus to the card for keyboard navigation
                                focusRequester.requestFocus()
                            }
                        ) {
                            Text("Cancel")
                        }
                    }
                } else {
                    // Normal view mode UI
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(currentCard.value.front, modifier = Modifier.weight(1f))
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit",
                            modifier = Modifier
                                .size(24.dp)
                                .clickable {
                                    mainViewModel.processAction(CardAction.Edit)
                                }
                        )
                    }
                    Divider()
                    if (isFlipped.value) {
                        Text(currentCard.value.back)
                    }
                }
            }
        }
    }
}
