package me.forketyfork.welk.components

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
import androidx.compose.material.AlertDialog
import androidx.compose.material.Button
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import co.touchlab.kermit.Logger
import kotlinx.coroutines.launch
import me.forketyfork.welk.presentation.CardAction
import me.forketyfork.welk.vm.CardInteractionManager
import me.forketyfork.welk.vm.DesktopCardAnimationManager
import me.forketyfork.welk.vm.DesktopCardViewModel
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

private val logger = Logger.withTag("CardPanel")

@Composable
fun CardPanel(modifier: Modifier = Modifier) {

    val cardInteractionManager = koinInject<CardInteractionManager>()
    val cardAnimationManager = koinInject<DesktopCardAnimationManager>()

    val cardViewModel = koinViewModel<DesktopCardViewModel>()

    val currentCard = cardViewModel.currentCard.collectAsStateWithLifecycle()
    val isFlipped = cardViewModel.isFlipped.collectAsStateWithLifecycle()
    val isEditing = cardViewModel.isEditing.collectAsStateWithLifecycle()
    val editCardContent = cardViewModel.editCardContent.collectAsStateWithLifecycle()
    val isDeleteConfirmationShowing =
        cardViewModel.isDeleteConfirmationShowing.collectAsStateWithLifecycle()
    val hasCards = cardViewModel.hasCards.collectAsStateWithLifecycle()

    var frontText by remember { mutableStateOf("") }
    var backText by remember { mutableStateOf("") }

    val coroutineScope = rememberCoroutineScope()

    // Update local state when edit mode is activated or when creating a new card
    LaunchedEffect(
        editCardContent.value,
        isEditing.value,
        cardViewModel.isNewCard.value
    ) {
        // Only update the text values if we're in edit mode
        if (isEditing.value) {
            val (front, back) = editCardContent.value
            frontText = front
            backText = back
        } else {
            // Clear the fields when exiting edit mode
            frontText = ""
            backText = ""
        }
    }

    val focusRequester = remember { FocusRequester() }

    // Request focus after UI is rendered and whenever card changes
    val currentDeck = cardViewModel.currentDeck.collectAsStateWithLifecycle()

    // Only request focus when we have cards or there's a deck selected
    LaunchedEffect(
        currentCard.value,
        currentDeck.value,
        hasCards.value
    ) {
        // Check if we have a deck and either have cards or are in edit mode
        if (currentDeck.value != null && (hasCards.value || isEditing.value)) {
            focusRequester.requestFocus()
        }
    }

    val animatedOffset by cardAnimationManager.animateOffset()
    val animatedColor by cardAnimationManager.animateColor()

    // Delete confirmation dialog
    if (isDeleteConfirmationShowing.value) {
        AlertDialog(
            onDismissRequest = { cardViewModel.processAction(CardAction.CancelDelete) },
            title = { Text("Delete Card") },
            text = { Text("Are you sure you want to delete this card? This action cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        coroutineScope.launch {
                            cardViewModel.processAction(CardAction.ConfirmDelete)
                        }
                    }
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { cardViewModel.processAction(CardAction.CancelDelete) }
                ) {
                    Text("Cancel")
                }
            }
        )
    }

    Box(
        // Make sure this panel can gain focus and receives keyboard events
        modifier = modifier
            .fillMaxSize()
            .testTag(CardPanelTestTags.CARD_PANEL)
            .onKeyEvent { event: KeyEvent ->
                logger.d { "Card got key event: $event" }
                val action = cardInteractionManager.handleKeyEvent(event)
                cardViewModel.processAction(action)
            }
            .focusRequester(focusRequester)
            .focusable(),
        contentAlignment = Alignment.Center
    ) {
        if (!hasCards.value && !isEditing.value) {
            // Show a message when there are no cards in the deck
            Column(
                modifier = Modifier
                    .width(315.dp)
                    .height(440.dp)
                    .border(width = 2.dp, color = Color.Black, shape = RoundedCornerShape(10.dp))
                    .background(color = Color.White)
                    .padding(all = 20.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    "No cards in this deck",
                    style = TextStyle(fontSize = 18.sp),
                    textAlign = TextAlign.Center,
                )
                Spacer(modifier = Modifier.height(20.dp))
                Button(
                    onClick = {
                        val currentDeck = cardViewModel.currentDeck.value
                        if (currentDeck != null) {
                            cardViewModel.processAction(CardAction.CreateNewCardInCurrentDeck)
                        }
                    },
                    modifier = Modifier.testTag(CardPanelTestTags.CREATE_FIRST_CARD_BUTTON)
                ) {
                    Text("Create a Card")
                }
            }
        } else {
            // Show the card
            Column(
                modifier = Modifier
                    .width(315.dp)
                    .height(440.dp)
                    .offset { animatedOffset }
                    .rotate(animatedOffset.x / 80.0f)
                    .border(width = 2.dp, color = Color.Black, shape = RoundedCornerShape(10.dp))
                    .background(color = animatedColor)
                    .padding(all = 20.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                if (isEditing.value) {
                    // Edit mode UI
                    OutlinedTextField(
                        value = frontText,
                        onValueChange = { frontText = it },
                        label = { Text("Front") },
                        modifier = Modifier.fillMaxWidth().weight(1f)
                            .testTag(CardPanelTestTags.EDIT_FRONT)
                    )
                    Divider()
                    OutlinedTextField(
                        value = backText,
                        onValueChange = { backText = it },
                        label = { Text("Back") },
                        modifier = Modifier.fillMaxWidth().weight(1f)
                            .testTag(CardPanelTestTags.EDIT_BACK)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        Button(
                            onClick = {
                                cardViewModel.updateEditContent(frontText, backText)
                                coroutineScope.launch {
                                    // First save the edits to update the card content
                                    cardViewModel.saveCardEdit()
                                    // Then exit edit mode
                                    cardViewModel.processAction(CardAction.SaveEdit)
                                    // Return focus to the card for keyboard navigation
                                    focusRequester.requestFocus()
                                }
                            },
                            modifier = Modifier.testTag(CardPanelTestTags.EDIT_SAVE)
                        ) {
                            Text("Save")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                // Cancel without saving changes and reset to original values
                                cardViewModel.processAction(CardAction.CancelEdit)
                                // Return focus to the card for keyboard navigation
                                focusRequester.requestFocus()
                            },
                            modifier = Modifier.testTag(CardPanelTestTags.EDIT_CANCEL)
                        ) {
                            Text("Cancel")
                        }
                    }
                } else {
                    // Normal view mode UI
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,

                        ) {
                        Text(
                            currentCard.value.front,
                            modifier = Modifier.weight(1f)
                        )
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Edit",
                                modifier = Modifier
                                    .size(24.dp)
                                    .clickable {
                                        cardViewModel.processAction(CardAction.Edit)
                                    }
                                    .testTag(CardPanelTestTags.EDIT_BUTTON)
                            )
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete",
                                modifier = Modifier
                                    .size(24.dp)
                                    .clickable {
                                        cardViewModel.processAction(CardAction.Delete)
                                    }
                                    .testTag(CardPanelTestTags.DELETE_BUTTON)
                            )
                        }
                    }
                    Divider()
                    if (isFlipped.value) {
                        Text(
                            text = currentCard.value.back,
                            modifier = Modifier.testTag(CardPanelTestTags.VIEW_BACK)
                        )
                    }
                }
            }
        }
    }
}

object CardPanelTestTags {
    const val VIEW_BACK = "card_view_back"
    const val EDIT_FRONT = "card_edit_front"
    const val EDIT_BACK = "card_edit_back"
    const val EDIT_SAVE = "card_edit_save_button"
    const val EDIT_CANCEL = "card_edit_cancel_button"
    const val EDIT_BUTTON = "card_edit_button"
    const val DELETE_BUTTON = "card_delete_button"
    const val CREATE_FIRST_CARD_BUTTON = "create_first_card_button"
    const val CARD_PANEL = "card_panel"
}
