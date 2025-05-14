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
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import me.forketyfork.welk.MainViewModel
import me.forketyfork.welk.domain.Deck
import me.forketyfork.welk.presentation.CardAction

object SidePanelTestTags {
    const val APP_TITLE = "app_title"
}

@Composable
fun SidePanel(
    mainViewModel: MainViewModel,
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
            color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f)
        )

        Spacer(modifier = Modifier.height(8.dp))

        // List of decks
        decks.forEach { deck ->
            DeckItem(
                deck = deck,
                isSelected = currentDeck?.id == deck.id,
                onClick = {
                    mainViewModel.viewModelScope.launch {
                        mainViewModel.selectDeck(deck.id)
                    }
                },
                onAddCard = { deckId ->
                    mainViewModel.processAction(CardAction.CreateNewCard(deckId))
                }
            )
        }
    }
}
