package me.forketyfork.welk.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import me.forketyfork.welk.presentation.CardAction
import me.forketyfork.welk.vm.DesktopCardViewModel
import me.forketyfork.welk.vm.DesktopLoginViewModel
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun SidePanel(
    width: Int = 250,
    modifier: Modifier = Modifier,
) {

    val loginViewModel = koinViewModel<DesktopLoginViewModel>()
    val cardViewModel = koinViewModel<DesktopCardViewModel>()

    val decks by cardViewModel.availableDecks.collectAsState()
    val currentDeck by cardViewModel.currentDeck.collectAsState()

    Box(
        modifier = modifier
            .width(width.dp)
            .fillMaxHeight()
            .background(MaterialTheme.colors.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
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
                    isSelected = currentDeck?.value?.id == deck.value.id,
                    onClick = {
                        cardViewModel.viewModelScope.launch {
                            cardViewModel.selectDeck(deck.value.id)
                        }
                    },
                    onAddCard = { deckId ->
                        cardViewModel.processAction(CardAction.CreateNewCard(deckId))
                    }
                )
            }

            // Spacer that pushes the bottom panel to the bottom
            Spacer(modifier = Modifier.weight(1f))

            // Bottom panel with actions
            Divider()
            Spacer(modifier = Modifier.height(8.dp))

            // Logout action
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                IconButton(
                    onClick = {
                        cardViewModel.viewModelScope.launch {
                            loginViewModel.signOut()
                        }
                    },
                    modifier = Modifier.testTag(SidePanelTestTags.LOGOUT_BUTTON)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                        contentDescription = "Logout",
                        tint = MaterialTheme.colors.primary
                    )
                }
            }
        }
    }
}

object SidePanelTestTags {
    const val APP_TITLE = "app_title"
    const val DECK_LIST_TITLE = "deck_list_title"
    const val LOGOUT_BUTTON = "logout_button"
}
