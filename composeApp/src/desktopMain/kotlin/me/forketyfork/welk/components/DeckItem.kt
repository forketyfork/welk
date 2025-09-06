package me.forketyfork.welk.components

import androidx.compose.foundation.TooltipArea
import androidx.compose.foundation.TooltipPlacement
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.flow.StateFlow
import me.forketyfork.welk.domain.Deck

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
) {
    val deckState by deck.collectAsStateWithLifecycle()
    val deckId = deckState.id ?: ""
    val isExpanded = expandedDeckIds.contains(deckId)

    Column {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(if (isSelected) MaterialTheme.colors.primary.copy(alpha = 0.1f) else Color.Transparent)
                .clickable { onClick() }
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
                    expandedDeckIds = expandedDeckIds
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
