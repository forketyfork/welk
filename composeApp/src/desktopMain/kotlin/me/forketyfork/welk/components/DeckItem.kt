package me.forketyfork.welk.components

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.*
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.flow.StateFlow
import me.forketyfork.welk.domain.Deck

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DeckItem(
    deck: StateFlow<Deck>,
    isSelected: Boolean,
    onClick: () -> Unit,
    onAddCard: ((String) -> Unit),
    onAddDeck: ((String) -> Unit),
    onDeleteDeck: ((String) -> Unit),
    childDecks: List<StateFlow<Deck>> = emptyList(),
    level: Int = 0,
    onChildDeckSelected: ((String) -> Unit) = {},
    onChildAddCard: ((String) -> Unit) = {},
    onChildAddDeck: ((String) -> Unit) = {},
    onChildDeleteDeck: ((String) -> Unit) = {},
    allDecks: List<StateFlow<Deck>> = emptyList(),
    onToggleExpansion: ((String) -> Unit) = {},
    expandedDeckIds: Set<String> = emptySet(),
    onMoveDeck: ((String, String?) -> Unit) = { _, _ -> },
    draggingDeckId: String? = null,
    dropTargetDeckId: String? = null,
    onDragStart: ((String) -> Unit) = {},
    onDragEnd: (() -> Unit) = {},
    onDeckPositioned: ((String, Offset, Offset) -> Unit) = { _, _, _ -> },
) {
    val deckState by deck.collectAsStateWithLifecycle()
    val deckId = deckState.id ?: ""
    val isExpanded = expandedDeckIds.contains(deckId)
    val isDragging = draggingDeckId == deckId
    val isDropTarget = dropTargetDeckId == deckId
    val isAnyDragActive = draggingDeckId != null

    Column {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
                .onGloballyPositioned { coordinates ->
                    // Report this deck's position for drop target detection
                    val topLeft = coordinates.localToWindow(Offset.Zero)
                    val bottomRight = coordinates.localToWindow(
                        Offset(
                            coordinates.size.width.toFloat(),
                            coordinates.size.height.toFloat()
                        )
                    )
                    onDeckPositioned(deckId, topLeft, bottomRight)
                }
                .clip(RoundedCornerShape(8.dp))
                .background(
                    when {
                        isSelected -> MaterialTheme.colors.primary.copy(alpha = 0.1f)
                        else -> Color.Transparent
                    }
                )
                .border(
                    width = when {
                        isDragging -> 2.dp
                        isDropTarget -> 2.dp
                        else -> 0.dp
                    },
                    color = when {
                        isDragging -> MaterialTheme.colors.primary.copy(alpha = 0.7f)
                        isDropTarget -> MaterialTheme.colors.secondary
                        else -> Color.Transparent
                    },
                    shape = RoundedCornerShape(8.dp)
                )
                .zIndex(if (isDragging) 1f else 0f)
                .clickable(enabled = !isAnyDragActive) { onClick() }
                .then(
                    // Only apply drag detection if this deck is not already being dragged
                    if (!isDragging && !isAnyDragActive) {
                        Modifier.pointerInput(deckId) {
                            var isPressed = false
                            
                            awaitPointerEventScope {
                                while (true) {
                                    val event = awaitPointerEvent()
                                    val change = event.changes.firstOrNull() ?: continue
                                    
                                    when (event.type) {
                                        PointerEventType.Press -> {
                                            if (change.pressed) {
                                                isPressed = true
                                                onDragStart(deckId)
                                            }
                                        }
                                        PointerEventType.Release -> {
                                            if (isPressed) {
                                                onDragEnd()
                                                isPressed = false
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        Modifier
                    }
                )
                .padding(start = (12 + level * 16).dp, end = 12.dp, top = 8.dp, bottom = 8.dp)
                .testTag(DeckItemTestTags.DECK_NAME_TEMPLATE.format(deckState.id))
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Expand/collapse icon for decks with children
                if (childDecks.isNotEmpty()) {
                    IconButton(
                        onClick = {
                            onToggleExpansion(deckId)
                        },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = if (isExpanded) Icons.Default.KeyboardArrowDown else Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            contentDescription = if (isExpanded) "Collapse" else "Expand",
                            tint = MaterialTheme.colors.onSurface,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                } else {
                    // Spacer to maintain alignment
                    Spacer(modifier = Modifier.width(24.dp))
                }

                // The deck name with a tooltip for full text
                Box(
                    modifier = Modifier.weight(1f)
                ) {
                    TooltipArea(
                        tooltip = {
                            Surface(
                                elevation = 4.dp,
                                shape = RoundedCornerShape(4.dp),
                                color = MaterialTheme.colors.surface
                            ) {
                                Box(
                                    modifier = Modifier
                                        .padding(8.dp)
                                ) {
                                    Text(
                                        text = deckState.name,
                                        color = MaterialTheme.colors.onSurface
                                    )
                                }
                            }
                        },
                        delayMillis = 600,
                        tooltipPlacement = TooltipPlacement.CursorPoint(
                            alignment = Alignment.TopStart
                        )
                    ) {
                        Text(
                            text = deckState.name,
                            style = MaterialTheme.typography.body1,
                            color = if (isSelected) MaterialTheme.colors.primary else MaterialTheme.colors.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // Action buttons
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.End
                ) {
                    // "Add deck" icon button
                    IconButton(
                        onClick = {
                            val deckId = deckState.id ?: error("Deck id is null for a persistent entity")
                            onAddDeck(deckId)
                        },
                        modifier = Modifier
                            .size(28.dp)
                            .testTag(DeckItemTestTags.ADD_DECK_BUTTON_TEMPLATE.format(deckState.id))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add Deck",
                            tint = MaterialTheme.colors.secondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    // "Add card" icon button
                    IconButton(
                        onClick = {
                            val deckId = deckState.id ?: error("Deck id is null for a persistent entity")
                            onAddCard(deckId)
                        },
                        modifier = Modifier
                            .size(28.dp)
                            .testTag(DeckItemTestTags.ADD_CARD_BUTTON_TEMPLATE.format(deckState.id))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add Card",
                            tint = MaterialTheme.colors.primary,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    // Delete deck icon button
                    IconButton(
                        onClick = {
                            val deckId = deckState.id ?: error("Deck id is null for a persistent entity")
                            onDeleteDeck(deckId)
                        },
                        modifier = Modifier
                            .size(28.dp)
                            .testTag(DeckItemTestTags.DELETE_DECK_BUTTON_TEMPLATE.format(deckState.id))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete Deck",
                            tint = MaterialTheme.colors.error,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }

        // Render child decks if expanded
        if (isExpanded && childDecks.isNotEmpty()) {
            childDecks.forEach { childDeck ->
                val childDeckState by childDeck.collectAsStateWithLifecycle()
                // Find grandchild decks for this child deck
                val childDecksOfChild = allDecks.filter { deck -> deck.value.parentId == childDeckState.id }

                DeckItem(
                    deck = childDeck,
                    isSelected = isSelected && childDeckState.id == deckState.id,
                    onClick = {
                        childDeckState.id?.let { onChildDeckSelected(it) }
                    },
                    onAddCard = onChildAddCard,
                    onAddDeck = onChildAddDeck,
                    onDeleteDeck = onChildDeleteDeck,
                    childDecks = childDecksOfChild,
                    level = level + 1,
                    onChildDeckSelected = onChildDeckSelected,
                    onChildAddCard = onChildAddCard,
                    onChildAddDeck = onChildAddDeck,
                    onChildDeleteDeck = onChildDeleteDeck,
                    allDecks = allDecks,
                    onToggleExpansion = onToggleExpansion,
                    expandedDeckIds = expandedDeckIds,
                    onMoveDeck = onMoveDeck,
                    draggingDeckId = draggingDeckId,
                    dropTargetDeckId = dropTargetDeckId,
                    onDragStart = onDragStart,
                    onDragEnd = onDragEnd,
                    onDeckPositioned = onDeckPositioned
                )
            }
        }
    }
}

object DeckItemTestTags {
    const val DECK_NAME_TEMPLATE = "deck_name_%s"
    const val ADD_CARD_BUTTON_TEMPLATE = "add_card_%s"
    const val ADD_DECK_BUTTON_TEMPLATE = "add_deck_%s"
    const val DELETE_DECK_BUTTON_TEMPLATE = "delete_deck_%s"
}
