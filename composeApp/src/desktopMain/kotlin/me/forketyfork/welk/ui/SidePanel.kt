package me.forketyfork.welk.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.Divider
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import me.forketyfork.welk.DesktopCardViewModel
import me.forketyfork.welk.presentation.CardAction

object SidePanelTestTags {
    const val APP_TITLE = "app_title"
    const val DECK_LIST_TITLE = "deck_list_title"
}

@Composable
fun SidePanel(
    cardViewModel: DesktopCardViewModel,
    width: Int = 250,
    modifier: Modifier = Modifier
) {
    val decks by cardViewModel.availableDecks.collectAsState()
    val currentDeck by cardViewModel.currentDeck.collectAsState()

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
                .testTag(SidePanelTestTags.APP_TITLE),
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
            color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f),
            modifier = Modifier.testTag(SidePanelTestTags.DECK_LIST_TITLE)
        )

        Spacer(modifier = Modifier.height(8.dp))

        // List of decks
        decks.forEach { deck ->
            DeckItem(
                deck = deck,
                isSelected = currentDeck?.id == deck.id,
                onClick = {
                    cardViewModel.viewModelScope.launch {
                        cardViewModel.selectDeck(deck.id)
                    }
                },
                onAddCard = { deckId ->
                    cardViewModel.processAction(CardAction.CreateNewCard(deckId))
                }
            )
        }
    }
}
