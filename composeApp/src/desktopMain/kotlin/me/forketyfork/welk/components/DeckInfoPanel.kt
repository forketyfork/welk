package me.forketyfork.welk.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Divider
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import me.forketyfork.welk.domain.Deck
import me.forketyfork.welk.theme.AppTheme

@Composable
fun DeckInfoPanel(
    deck: Deck,
    totalCount: Int,
    reviewedCount: Int,
    dueCount: Int,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .testTag(DeckInfoPanelTestTags.DECK_INFO_PANEL),
    ) {
        Divider()
        Spacer(modifier = Modifier.height(8.dp))
        if (deck.description.isNotEmpty()) {
            Text(
                deck.description,
                style = AppTheme.typography.body2,
                color = AppTheme.colors.textDisabled,
            )
            Spacer(modifier = Modifier.height(4.dp))
        }
        Text(
            "$totalCount cards, $reviewedCount reviewed, $dueCount due",
            style = AppTheme.typography.caption,
            color = AppTheme.colors.textDisabled,
        )
    }
}

object DeckInfoPanelTestTags {
    const val DECK_INFO_PANEL = "deck_info_panel"
}
