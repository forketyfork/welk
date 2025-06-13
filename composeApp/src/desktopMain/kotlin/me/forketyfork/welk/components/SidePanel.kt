package me.forketyfork.welk.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Add
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import me.forketyfork.welk.presentation.CardAction
import me.forketyfork.welk.vm.DesktopCardViewModel
import me.forketyfork.welk.vm.DesktopLoginViewModel
import me.forketyfork.welk.vm.ThemeViewModel
import org.koin.compose.viewmodel.koinViewModel
import androidx.compose.desktop.ui.tooling.preview.Preview

@Preview
@Composable
fun SidePanel(
    width: Int = 250,
    modifier: Modifier = Modifier,
) {

    val loginViewModel = koinViewModel<DesktopLoginViewModel>()
    val cardViewModel = koinViewModel<DesktopCardViewModel>()
    val themeViewModel = koinViewModel<ThemeViewModel>()

    val decks by cardViewModel.availableDecks.collectAsStateWithLifecycle()
    val currentDeck by cardViewModel.currentDeck.collectAsStateWithLifecycle()
    val themeMode by themeViewModel.themeMode.collectAsStateWithLifecycle()
    val expandedDeckIds by cardViewModel.expandedDeckIds.collectAsStateWithLifecycle()

    var showAddDeckDialog by remember { mutableStateOf(false) }
    var newDeckName by remember { mutableStateOf("") }
    var newDeckDescription by remember { mutableStateOf("") }
    var parentDeckId by remember { mutableStateOf<String?>(null) }
    val deckNameFocusRequester = remember { FocusRequester() }
    var deckIdToDelete by remember { mutableStateOf<String?>(null) }
    val deckListScrollState = rememberScrollState()


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

            // Scrollable list of decks
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(deckListScrollState)
            ) {
                // Filter top-level decks (those with no parent)
                val topLevelDecks = decks.filter { it.value.parentId == null }

                topLevelDecks.forEach { deck ->
                    // Find child decks for this deck
                    val childDecks = decks.filter { it.value.parentId == deck.value.id }

                    DeckItem(
                        deck = deck,
                        isSelected = currentDeck?.value?.id == deck.value.id,
                        onClick = {
                            cardViewModel.viewModelScope.launch {
                                val deckId = deck.value.id ?: error("Deck id is null for a persistent entity")
                                cardViewModel.selectDeck(deckId)
                            }
                        },
                        onAddCard = { deckId ->
                            cardViewModel.processAction(CardAction.CreateNewCard(deckId))
                        },
                        onAddDeck = { deckId ->
                            parentDeckId = deckId
                            showAddDeckDialog = true
                        },
                        onDeleteDeck = { id -> deckIdToDelete = id },
                        childDecks = childDecks,
                        onChildDeckSelected = { childDeckId ->
                            cardViewModel.viewModelScope.launch {
                                cardViewModel.selectDeck(childDeckId)
                            }
                        },
                        onChildAddCard = { deckId ->
                            cardViewModel.processAction(CardAction.CreateNewCard(deckId))
                        },
                        onChildAddDeck = { deckId ->
                            parentDeckId = deckId
                            showAddDeckDialog = true
                        },
                        onChildDeleteDeck = { id -> deckIdToDelete = id },
                        allDecks = decks,
                        onToggleExpansion = { deckId ->
                            cardViewModel.toggleDeckExpansion(deckId)
                        },
                        expandedDeckIds = expandedDeckIds
                    )
                }
            }

            // Bottom panel with actions
            Divider()
            Spacer(modifier = Modifier.height(8.dp))

            // Bottom action buttons panel with fixed height
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .padding(vertical = 8.dp)
            ) {
                // Theme toggle button
                IconButton(
                    onClick = {
                        themeViewModel.toggleThemeMode()
                    },
                    modifier = Modifier.testTag(SidePanelTestTags.THEME_TOGGLE_BUTTON)
                ) {
                    Icon(
                        imageVector = themeMode.icon,
                        contentDescription = themeMode.contentDescription,
                        tint = MaterialTheme.colors.primary
                    )
                }

                // Add deck button
                IconButton(
                    onClick = { showAddDeckDialog = true },
                    modifier = Modifier.testTag(SidePanelTestTags.ADD_DECK_BUTTON)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add Deck",
                        tint = MaterialTheme.colors.primary
                    )
                }

                // Logout button
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

            if (showAddDeckDialog) {
                val dialogTitle = if (parentDeckId != null) {
                    val parentDeck = decks.find { it.value.id == parentDeckId }?.value
                    "Add Deck to ${parentDeck?.name ?: "Parent"}"
                } else {
                    "Add Top-Level Deck"
                }

                AlertDialog(
                    onDismissRequest = {
                        showAddDeckDialog = false
                        parentDeckId = null
                    },
                    title = { Text(dialogTitle) },
                    text = {
                        Column {
                            OutlinedTextField(
                                value = newDeckName,
                                onValueChange = { newDeckName = it },
                                label = { Text("Name") },
                                modifier = Modifier.fillMaxWidth()
                                    .focusRequester(deckNameFocusRequester)
                                    .testTag(SidePanelTestTags.NEW_DECK_NAME),
                                singleLine = true,
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = newDeckDescription,
                                onValueChange = { newDeckDescription = it },
                                label = { Text("Description") },
                                modifier = Modifier.fillMaxWidth()
                                    .testTag(SidePanelTestTags.NEW_DECK_DESCRIPTION),
                                singleLine = true,
                            )

                            LaunchedEffect(Unit) {
                                deckNameFocusRequester.requestFocus()
                            }
                        }
                    },
                    confirmButton = {
                        TextButton(
                            modifier = Modifier.testTag(SidePanelTestTags.SAVE_DECK_BUTTON),
                            onClick = {
                                val newDeckNameValue = newDeckName
                                val newDeckDescriptionValue = newDeckDescription
                                val parentId = parentDeckId
                                cardViewModel.viewModelScope.launch {
                                    cardViewModel.createDeck(newDeckNameValue, newDeckDescriptionValue, parentId)
                                }
                                newDeckName = ""
                                newDeckDescription = ""
                                parentDeckId = null
                                showAddDeckDialog = false
                            }
                        ) { Text("Save") }
                    },
                    dismissButton = {
                        TextButton(onClick = {
                            showAddDeckDialog = false
                            parentDeckId = null
                        }) { Text("Cancel") }
                    }
                )
            }

            if (deckIdToDelete != null) {
                AlertDialog(
                    onDismissRequest = { deckIdToDelete = null },
                    title = { Text("Delete Deck") },
                    text = { Text("Are you sure you want to delete this deck? This action cannot be undone.") },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                val id = deckIdToDelete
                                if (id != null) {
                                    cardViewModel.viewModelScope.launch {
                                        cardViewModel.deleteDeck(id)
                                    }
                                }
                                deckIdToDelete = null
                            },
                            modifier = Modifier.testTag(SidePanelTestTags.CONFIRM_DELETE_BUTTON)
                        ) { Text("Delete") }
                    },
                    dismissButton = {
                        TextButton(onClick = { deckIdToDelete = null }) { Text("Cancel") }
                    }
                )
            }
        }
    }
}

object SidePanelTestTags {
    const val APP_TITLE = "app_title"
    const val DECK_LIST_TITLE = "deck_list_title"
    const val LOGOUT_BUTTON = "logout_button"
    const val THEME_TOGGLE_BUTTON = "theme_toggle_button"
    const val ADD_DECK_BUTTON = "add_deck_button"
    const val NEW_DECK_NAME = "new_deck_name"
    const val NEW_DECK_DESCRIPTION = "new_deck_description"
    const val SAVE_DECK_BUTTON = "save_deck_button"
    const val CONFIRM_DELETE_BUTTON = "confirm_delete_button"
}
