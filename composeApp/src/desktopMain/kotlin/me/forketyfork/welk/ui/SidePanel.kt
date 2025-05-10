package me.forketyfork.welk.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Divider
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
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
                .padding(bottom = 16.dp),
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
                onClick = { onDeckSelected(deck) }
            )
        }
    }
}

@Composable
private fun DeckItem(
    deck: Deck,
    isSelected: Boolean,
    onClick: () -> Unit
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
        // Card count positioned to the right
        Text(
            text = "${deck.cardCount} cards",
            style = MaterialTheme.typography.caption,
            color = MaterialTheme.colors.onSurface.copy(alpha = 0.5f),
            modifier = Modifier.align(Alignment.TopEnd)
        )

        // Column for deck main content with right padding to avoid overlapping the count
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(end = 70.dp) // Make space for the card count
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