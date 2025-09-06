package me.forketyfork.welk.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.key.*
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import co.touchlab.kermit.Logger
import kotlinx.coroutines.launch
import me.forketyfork.welk.presentation.CardAction
import me.forketyfork.welk.theme.AppTheme
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

    var frontText by remember { mutableStateOf(TextFieldValue("")) }
    var backText by remember { mutableStateOf(TextFieldValue("")) }

    val coroutineScope = rememberCoroutineScope()

    // Update the local state when edit mode is activated or when creating a new card
    LaunchedEffect(
        editCardContent.value,
        isEditing.value,
        cardViewModel.isNewCard.value
    ) {
        // Only update the text values if we're in edit mode
        if (isEditing.value) {
            val (front, back) = editCardContent.value
            frontText = TextFieldValue(front, TextRange(front.length))
            backText = TextFieldValue(back, TextRange(back.length))
        } else {
            // Clear the fields when exiting edit mode
            frontText = TextFieldValue("")
            backText = TextFieldValue("")
        }
    }

    val focusRequester = remember { FocusRequester() }
    val editFrontFocusRequester = remember { FocusRequester() }

    val currentDeck = cardViewModel.currentDeck.collectAsStateWithLifecycle()
    val currentCards = cardViewModel.currentDeckCards.collectAsStateWithLifecycle()
    val hasCards = remember(currentCards.value) {
        derivedStateOf {
            currentCards.value.isNotEmpty()
        }
    }

    val reviewedCardCount = cardViewModel.reviewedCardCount.collectAsStateWithLifecycle()
    val dueCardCount = cardViewModel.dueCardCount.collectAsStateWithLifecycle()
    val totalCardCount = cardViewModel.totalCardCount.collectAsStateWithLifecycle()
    val showAllCards = cardViewModel.showAllCards.collectAsStateWithLifecycle()

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

    LaunchedEffect(isEditing.value) {
        if (isEditing.value) {
            editFrontFocusRequester.requestFocus()
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
                    },
                    modifier = Modifier.testTag(CardPanelTestTags.CONFIRM_DELETE_BUTTON)
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
        modifier = modifier
            .fillMaxSize()
            .background(AppTheme.colors.transparent)
            .testTag(CardPanelTestTags.CARD_PANEL)
            .onPreviewKeyEvent { event: KeyEvent ->
                logger.d { "Card got key event: $event" }

                val action = cardInteractionManager.handleKeyEvent(event)
                cardViewModel.processAction(action)
            }
            .focusRequester(focusRequester)
            .focusable()
    ) {
        // Show all cards checkbox in the top-right corner
        Row(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = showAllCards.value,
                onCheckedChange = { cardViewModel.toggleShowAllCards() }
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "Show all",
                style = AppTheme.typography.body2,
                color = AppTheme.colors.onSurface
            )
        }
        // Main card content centered in the full available space
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            if (currentDeck.value != null) {
                if (!hasCards.value && !isEditing.value) {
                    // Show a message when there are no cards in the deck
                    Column(
                        modifier = Modifier
                            .width(315.dp)
                            .height(440.dp)
                            .border(
                                width = 2.dp,
                                color = AppTheme.colors.primary,
                                shape = RoundedCornerShape(10.dp)
                            )
                            .background(color = AppTheme.colors.transparent)
                            .padding(all = 20.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        // Add flexible spacer to center content vertically
                        Spacer(modifier = Modifier.weight(1f))

                        Text(
                            "No cards in this deck",
                            style = TextStyle(fontSize = 18.sp),
                            textAlign = TextAlign.Center,
                            color = AppTheme.colors.onSurface
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

                        // Add flexible spacer to center content vertically
                        Spacer(modifier = Modifier.weight(1f))
                    }
                } else {
                    // Show the card
                    Column(
                        modifier = Modifier
                            .width(315.dp)
                            .height(440.dp)
                            .offset { animatedOffset }
                            .rotate(animatedOffset.x / 80.0f)
                            .border(
                                width = 2.dp,
                                color = AppTheme.colors.primary,
                                shape = RoundedCornerShape(10.dp)
                            )
                            .background(color = animatedColor)
                            .padding(all = 20.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        if (isEditing.value) {
                            // Edit mode UI
                            OutlinedTextField(
                                value = frontText,
                                colors = TextFieldDefaults.textFieldColors(
                                    textColor = AppTheme.colors.onSurface
                                ),
                                onValueChange = { frontText = it },
                                label = { Text("Front") },
                                modifier = Modifier.fillMaxWidth().weight(1f)
                                    .focusRequester(editFrontFocusRequester)
                                    .testTag(CardPanelTestTags.EDIT_FRONT)
                                    .moveFocusOnTab()
                            )
                            Divider()
                            OutlinedTextField(
                                value = backText,
                                colors = TextFieldDefaults.textFieldColors(
                                    textColor = AppTheme.colors.onSurface
                                ),
                                onValueChange = { backText = it },
                                label = { Text("Back") },
                                modifier = Modifier.fillMaxWidth().weight(1f)
                                    .testTag(CardPanelTestTags.EDIT_BACK).moveFocusOnTab()
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                Button(
                                    onClick = {
                                        cardViewModel.updateEditContent(frontText.text, backText.text)
                                        coroutineScope.launch {
                                            // First, save the edits to update the card content
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
                            val card = currentCard.value
                            if (card != null) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,

                                    ) {
                                    Text(
                                        card.front,
                                        modifier = Modifier.weight(1f),
                                        color = AppTheme.colors.onSurface
                                    )
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Edit,
                                            contentDescription = "Edit",
                                            tint = AppTheme.colors.primary,
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
                                            tint = AppTheme.colors.error,
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
                                        text = card.back,
                                        color = AppTheme.colors.onSurface,
                                        modifier = Modifier.testTag(CardPanelTestTags.VIEW_BACK)
                                    )
                                }

                                // A review status indicator in the bottom-right corner
                                Spacer(modifier = Modifier.weight(1f))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End
                                ) {
                                    ReviewStatusIndicator(
                                        nextReview = card.nextReview
                                    )
                                }
                            }
                        }
                    }
                }
            }

        }

        // Grade buttons positioned below the card area
        if (hasCards.value && !isEditing.value && currentCard.value != null) {
            GradeButtons(
                onGrade = { grade ->
                    coroutineScope.launch {
                        cardViewModel.gradeCard(grade)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 80.dp) // Space for DeckInfoPanel below
            )
        }

        // Position the DeckInfoPanel at the bottom, independent of card positioning
        currentDeck.value?.value?.let { deck ->
            DeckInfoPanel(
                deck = deck,
                totalCount = totalCardCount.value,
                reviewedCount = reviewedCardCount.value,
                dueCount = dueCardCount.value,
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }
    }
}

// For multiline fields, Tab doesn't work as expected, so we need to move focus manually
// see https://github.com/JetBrains/compose-multiplatform/blob/master/tutorials/Tab_Navigation/README.md#a-possible-workaround
fun Modifier.moveFocusOnTab() = composed {
    val focusManager = LocalFocusManager.current
    onPreviewKeyEvent {
        if (it.type == KeyEventType.KeyDown && it.key == Key.Tab) {
            focusManager.moveFocus(
                if (it.isShiftPressed) FocusDirection.Previous else FocusDirection.Next
            )
            true
        } else {
            false
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
    const val CONFIRM_DELETE_BUTTON = "confirm_delete_button"
}
