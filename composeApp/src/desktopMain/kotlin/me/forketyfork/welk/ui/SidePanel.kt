package me.forketyfork.welk.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import me.forketyfork.welk.presentation.CardAction
import me.forketyfork.welk.MainViewModel
import me.forketyfork.welk.domain.Deck

@Composable
fun SidePanel(
    mainViewModel: MainViewModel,
    onDeckSelected: (Deck) -> Unit,
    width: Int = 250,
    modifier: Modifier = Modifier
) {
    val decks by mainViewModel.availableDecks.collectAsState()
    val currentDeck by mainViewModel.currentDeck.collectAsState()

    Column(
        modifier = modifier
            .width(width.dp)
            .fillMaxHeight()
            .background(MaterialTheme.colors.surface)
            .padding(16.dp)
    ) {
        // App name with custom font
        Text(
            text = "Welk\uD83C\uDF42",
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
                .testTag("app_title"),
            style = MaterialTheme.typography.h1,
            color = MaterialTheme.colors.primary,
            textAlign = TextAlign.Center
        )

        Divider()
        Spacer(modifier = Modifier.height(16.dp))

        // Deck list title
        Text(
            text = "Decks",
            style = MaterialTheme.typography.h3,
            color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f)
        )

        Spacer(modifier = Modifier.height(8.dp))

        // List of decks
        decks.forEach { deck ->
            DeckItem(
                deck = deck,
                isSelected = currentDeck?.id == deck.id,
                onClick = { onDeckSelected(deck) },
                onAddCard = { deckId ->
                    mainViewModel.processAction(CardAction.CreateNewCard(deckId))
                }
            )
        }
    }
}

@Composable
private fun DeckItem(
    deck: Deck,
    isSelected: Boolean,
    onClick: () -> Unit,
    onAddCard: ((String) -> Unit)? = null
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(if (isSelected) MaterialTheme.colors.primary.copy(alpha = 0.1f) else Color.Transparent)
            .clickable { onClick() }
            .padding(12.dp)
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
                    modifier = Modifier.height(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add Card",
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(
                        "Add Card",
                        style = MaterialTheme.typography.caption
                    )
                }
            }
        }

        // Column for deck main content with right padding to avoid overlapping the controls
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(end = 90.dp) // Make space for the controls
        ) {
            Text(
                text = deck.name,
                style = MaterialTheme.typography.body1,
                color = if (isSelected) MaterialTheme.colors.primary else MaterialTheme.colors.onSurface
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