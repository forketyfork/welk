package me.forketyfork.welk.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextButton
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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.flow.StateFlow
import me.forketyfork.welk.domain.Deck

@Composable
fun DeckItem(
    deck: StateFlow<Deck>,
    isSelected: Boolean,
    onClick: () -> Unit,
    onAddCard: ((String) -> Unit)? = null,
    onDeleteDeck: ((String) -> Unit)? = null
) {
    val deck by deck.collectAsStateWithLifecycle()

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(if (isSelected) MaterialTheme.colors.primary.copy(alpha = 0.1f) else Color.Transparent)
            .clickable { onClick() }
            .padding(12.dp)
            .testTag(DeckItemTestTags.DECK_NAME_TEMPLATE.format(deck.id))
    ) {
        // Right-side controls column
        Column(
            modifier = Modifier.align(Alignment.TopEnd),
            horizontalAlignment = Alignment.End
        ) {
            // Card count
            Text(
                text = "${deck.cardCount} cards",
                style = MaterialTheme.typography.caption,
                color = MaterialTheme.colors.onSurface.copy(alpha = 0.5f)
            )

            // Spacer between count and add button
            Spacer(modifier = Modifier.height(4.dp))

            // Add card button
            if (onAddCard != null) {
                TextButton(
                    onClick = { onAddCard(deck.id) },
                    contentPadding = PaddingValues(
                        horizontal = 8.dp,
                        vertical = 2.dp
                    ),
                    modifier = Modifier
                        .height(24.dp)
                        .testTag(DeckItemTestTags.ADD_CARD_BUTTON_TEMPLATE.format(deck.id))
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add Card",
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colors.primary
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(
                        "Add Card",
                        style = MaterialTheme.typography.caption
                    )
                }
            }

            if (onDeleteDeck != null) {
                Spacer(modifier = Modifier.height(4.dp))
                TextButton(
                    onClick = { onDeleteDeck(deck.id) },
                    contentPadding = PaddingValues(
                        horizontal = 8.dp,
                        vertical = 2.dp
                    ),
                    modifier = Modifier
                        .height(24.dp)
                        .testTag(DeckItemTestTags.DELETE_DECK_BUTTON_TEMPLATE.format(deck.id))
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete Deck",
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colors.error
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(
                        "Delete",
                        style = MaterialTheme.typography.caption,
                        color = MaterialTheme.colors.error
                    )
                }
            }
        }

        // Column for deck main content with right padding to avoid overlapping the controls
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(end = 120.dp) // Make space for the controls

        ) {
            Text(
                text = deck.name,
                style = MaterialTheme.typography.body1,
                color = if (isSelected) MaterialTheme.colors.primary else MaterialTheme.colors.onSurface,
            )
            if (deck.description.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = deck.description,
                    style = MaterialTheme.typography.body2,
                    color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
                )
            }
        }
    }
}

object DeckItemTestTags {
    const val DECK_NAME_TEMPLATE = "deck_name_%s"
    const val ADD_CARD_BUTTON_TEMPLATE = "add_card_%s"
    const val DELETE_DECK_BUTTON_TEMPLATE = "delete_deck_%s"
}
