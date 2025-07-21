package me.forketyfork.welk

import androidx.compose.ui.input.key.Key
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.*
import me.forketyfork.welk.components.CardPanelTestTags
import me.forketyfork.welk.components.DeckItemTestTags
import org.junit.Test
import org.koin.test.KoinTest

class EditCardTest : KoinTest {

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun canEditExistingCard() = runComposeUiTest {
        val deckName = "Edit Deck ${System.currentTimeMillis()}"
        val deckDescription = "Deck for editing"

        setupApp()
        login("user@test", "password")

        var deckId: String? = null
        try {
            deckId = createTestDeck(deckName, deckDescription)
            createTestCard(deckId, "Front", "Back")

            onNodeWithTag(DeckItemTestTags.DECK_NAME_TEMPLATE.format(deckId)).performClick()
            waitUntilExactlyOneExists(hasTestTag(CardPanelTestTags.CARD_PANEL))

            editCurrentCard("Edited Front", "Edited Back")

            // verify flipped content works after edit
            onRoot().performKeyInput { pressKey(Key.Spacebar) }
            waitUntilExactlyOneExists(hasTextExactly("Edited Back"))
        } finally {
            deckId?.let { deleteTestDeck(it) }
            logout()
        }
    }
}
