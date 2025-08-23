package me.forketyfork.welk.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.Divider
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import me.forketyfork.welk.domain.Deck

@Composable
fun DeckInfoPanel(deck: Deck, totalCount: Int, reviewedCount: Int, dueCount: Int, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .testTag(DeckInfoPanelTestTags.DECK_INFO_PANEL)
    ) {
        Divider()
        Spacer(modifier = Modifier.height(8.dp))
        if (deck.description.isNotEmpty()) {
            Text(
                deck.description,
                style = MaterialTheme.typography.body2,
                color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
            )
            Spacer(modifier = Modifier.height(4.dp))
        }
        Text(
            "$totalCount cards, $reviewedCount reviewed, $dueCount due",
            style = MaterialTheme.typography.caption,
            color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
        )
    }
}

object DeckInfoPanelTestTags {
    const val DECK_INFO_PANEL = "deck_info_panel"
}
