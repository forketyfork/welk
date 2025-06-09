package me.forketyfork.welk.components

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
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

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DeckItem(
    deck: StateFlow<Deck>,
    isSelected: Boolean,
    onClick: () -> Unit,
    onAddCard: ((String) -> Unit),
    onDeleteDeck: ((String) -> Unit),
) {
    val deck by deck.collectAsStateWithLifecycle()

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(if (isSelected) MaterialTheme.colors.primary.copy(alpha = 0.1f) else Color.Transparent)
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 8.dp)
            .testTag(DeckItemTestTags.DECK_NAME_TEMPLATE.format(deck.id))
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
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
                                    text = deck.name,
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
                        text = deck.name,
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
                // "Add card" icon button
                IconButton(
                    onClick = {
                        val deckId = deck.id ?: error("Deck id is null for a persistent entity")
                        onAddCard(deckId)
                    },
                    modifier = Modifier
                        .size(28.dp)
                        .testTag(DeckItemTestTags.ADD_CARD_BUTTON_TEMPLATE.format(deck.id))
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
                        val deckId = deck.id ?: error("Deck id is null for a persistent entity")
                        onDeleteDeck(deckId)
                    },
                    modifier = Modifier
                        .size(28.dp)
                        .testTag(DeckItemTestTags.DELETE_DECK_BUTTON_TEMPLATE.format(deck.id))
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
}

object DeckItemTestTags {
    const val DECK_NAME_TEMPLATE = "deck_name_%s"
    const val ADD_CARD_BUTTON_TEMPLATE = "add_card_%s"
    const val DELETE_DECK_BUTTON_TEMPLATE = "delete_deck_%s"
}
