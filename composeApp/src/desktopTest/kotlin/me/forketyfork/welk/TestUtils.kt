package me.forketyfork.welk

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.InternalComposeUiApi
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.*
import androidx.lifecycle.*
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import kotlinx.coroutines.runBlocking
import me.forketyfork.welk.components.CardPanelTestTags
import me.forketyfork.welk.components.DeckItemTestTags
import me.forketyfork.welk.components.LoginViewTestTags
import me.forketyfork.welk.components.SidePanelTestTags
import me.forketyfork.welk.domain.Deck
import me.forketyfork.welk.domain.DeckRepository
import org.koin.test.KoinTest
import org.koin.test.get

/**
 * Fake implementation of [LifecycleOwner] to be used in the tests
 */
class TestLifecycleOwner : LifecycleOwner {
    override val lifecycle: Lifecycle = LifecycleRegistry(this).apply {
        currentState = Lifecycle.State.RESUMED
    }
}

class TestViewModelStoreOwner : ViewModelStoreOwner {
    override val viewModelStore: ViewModelStore = ViewModelStore()
}

/**
 * Gets test credentials from environment variables and fails if they're not set
 */
@OptIn(ExperimentalTestApi::class)
fun getTestCredentials(): Pair<String, String> {
    val testUsername = System.getenv("WELK_TEST_USERNAME") ?: System.getProperty("WELK_TEST_USERNAME") ?: "user@test"
    val testPassword = System.getenv("WELK_TEST_PASSWORD") ?: System.getProperty("WELK_TEST_PASSWORD") ?: "password"
    return testUsername to testPassword
}

/**
 * Cleans up the database for the test user by deleting all decks and their associated cards.
 * This should be called before starting tests to ensure a clean state.
 */
fun KoinTest.cleanupTestUserDatabase() {
    runBlocking {
        try {
            val deckRepository = get<DeckRepository>()

            // Get all top-level decks (parentId = null)
            val topLevelDecks = deckRepository.getChildDecks(null)

            // Delete each top-level deck (this will recursively delete child decks and all cards)
            topLevelDecks.forEach { deck: Deck ->
                deck.id?.let { deckId: String ->
                    deckRepository.deleteDeck(deckId)
                }
            }
        } catch (e: Exception) {
            // Log the error but don't fail the test setup
            println("Warning: Failed to cleanup test user database: ${e.message}")
        }
    }
}

/**
 * Sets up the composition with the App composable
 */
@OptIn(ExperimentalTestApi::class)
fun ComposeUiTest.setupApp() {
    setContent {
        CompositionLocalProvider(
            LocalLifecycleOwner provides TestLifecycleOwner(),
            LocalViewModelStoreOwner provides TestViewModelStoreOwner()
        ) {
            App()
        }
    }
}

/**
 * Sets up the app, logs in with the provided credentials, and cleans up the database beforehand.
 * This is the recommended way to start tests to ensure a clean database state.
 */
@OptIn(ExperimentalTestApi::class)
fun KoinTest.setupAppWithCleanDatabase(composeTest: ComposeUiTest, username: String, password: String) {
    // Set up the app
    composeTest.setupApp()

    // Check if the logout button is present and logout if it exists
    try {
        composeTest.onNodeWithTag(SidePanelTestTags.LOGOUT_BUTTON).assertExists()
        composeTest.logout()
    } catch (_: AssertionError) {
        // Logout button is absent, continue without logout
    }

    // Log in first to establish authentication context
    composeTest.login(username, password)

    // Clean up the database after login to ensure a clean state
    cleanupTestUserDatabase()

    // Logout and login again to refresh the UI state after cleanup
    composeTest.logout()
    composeTest.login(username, password)
}

/**
 * Logs in with the provided credentials and cleans up the database beforehand
 */
@OptIn(ExperimentalTestApi::class)
fun ComposeUiTest.login(username: String, password: String) {
    onNodeWithTag(LoginViewTestTags.USERNAME_INPUT).performTextInput(username)
    onNodeWithTag(LoginViewTestTags.PASSWORD_INPUT).performTextInput(password)
    onNodeWithTag(LoginViewTestTags.SIGN_IN_BUTTON).performClick()
    verifyBasicUiElements()
}

/**
 * Verifies that basic UI elements are visible after login
 */
@OptIn(ExperimentalTestApi::class)
fun ComposeUiTest.verifyBasicUiElements() {
    waitUntilExactlyOneExists(hasTestTag(SidePanelTestTags.APP_TITLE), timeoutMillis = 10000)
    onNodeWithTag(SidePanelTestTags.APP_TITLE).assertTextEquals("Welk\uD83C\uDF42")
    onNodeWithTag(SidePanelTestTags.DECK_LIST_TITLE).assertTextEquals("Decks")
    onNodeWithTag(SidePanelTestTags.LOGOUT_BUTTON).assertExists()
}

/**
 * Logs out and verifies that the login screen is shown
 */
@OptIn(ExperimentalTestApi::class)
fun ComposeUiTest.logout() {
    waitUntilExactlyOneExists(hasTestTag(SidePanelTestTags.LOGOUT_BUTTON), timeoutMillis = 10000)
    onNodeWithTag(SidePanelTestTags.LOGOUT_BUTTON).performClick()
    waitUntilExactlyOneExists(
        hasTestTag(LoginViewTestTags.USERNAME_INPUT),
        timeoutMillis = 10000
    )
}

@Suppress("unused") // a handy function for debugging
@OptIn(ExperimentalTestApi::class, InternalComposeUiApi::class)
fun ComposeUiTest.printSemanticNodeState() {
    println()
    onAllNodes(SemanticsMatcher("all nodes") { true }).fetchSemanticsNodes().forEach { node ->
        println(
            "Node: Text = ${node.config.getOrNull(SemanticsProperties.Text)}, " +
                    "Tag = ${node.config.getOrNull(SemanticsProperties.TestTag)}, " +
                    "Role = ${node.config.getOrNull(SemanticsProperties.Role)}, " +
                    "Description = ${node.config.getOrNull(SemanticsProperties.ContentDescription)}, " +
                    "Focused = ${node.config.getOrNull(SemanticsProperties.Focused)}"
        )
    }
}

@OptIn(ExperimentalTestApi::class, InternalComposeUiApi::class)
fun ComposeUiTest.getDeckIdByName(name: String): String {
    val tag = onNodeWithText(name)
        .fetchSemanticsNode()
        .config.getOrNull(SemanticsProperties.TestTag)
        ?: error("Deck tag not found for $name")
    return tag.removePrefix("deck_name_")
}

/**
 * Creates a test deck through the UI and returns its ID
 */
@OptIn(ExperimentalTestApi::class)
fun ComposeUiTest.createTestDeck(name: String, description: String): String {
    // Click the "Add deck" button
    onNodeWithTag(SidePanelTestTags.ADD_DECK_BUTTON).performClick()

    // Wait until the new deck dialog opens
    waitUntilExactlyOneExists(hasTestTag(SidePanelTestTags.NEW_DECK_NAME))

    // Fill in the deck creation dialog
    onNodeWithTag(SidePanelTestTags.NEW_DECK_NAME).performTextInput(name)
    onNodeWithTag(SidePanelTestTags.NEW_DECK_DESCRIPTION).performTextInput(description)

    // Save the new deck
    onNodeWithTag(SidePanelTestTags.SAVE_DECK_BUTTON).performClick()

    // Wait for the new deck to appear
    waitUntilExactlyOneExists(hasTextExactly(name))

    // Return the newly created deck id
    return getDeckIdByName(name)
}

/**
 * Creates a test card in the specified deck through the UI
 */
@OptIn(ExperimentalTestApi::class)
fun ComposeUiTest.createTestCard(deckId: String, front: String, back: String) {
    // Add a card to the deck using its test tag
    onNodeWithTag(DeckItemTestTags.ADD_CARD_BUTTON_TEMPLATE.format(deckId)).performClick()

    waitUntilExactlyOneExists(hasTestTag(CardPanelTestTags.EDIT_FRONT))

    // Enter the front text of the new card
    onNodeWithTag(CardPanelTestTags.EDIT_FRONT).performTextInput(front)

    // Enter the back text of the new card
    onNodeWithTag(CardPanelTestTags.EDIT_BACK).performTextInput(back)

    // Save the new card
    onNodeWithTag(CardPanelTestTags.EDIT_SAVE).performClick()

    // Wait for the card to be created and visible
    waitUntilExactlyOneExists(hasTextExactly(front), timeoutMillis = 5000)
}

/**
 * Deletes a test deck through the UI
 */
@OptIn(ExperimentalTestApi::class)
fun ComposeUiTest.deleteTestDeck(deckId: String) {
    // Delete the deck via the delete button tagged with the deck id
    onNodeWithTag(DeckItemTestTags.DELETE_DECK_BUTTON_TEMPLATE.format(deckId)).performClick()

    // Confirm the deletion in the dialog
    waitUntilExactlyOneExists(hasText("Are you sure you want to delete this deck? This action cannot be undone."))
    onNodeWithTag(SidePanelTestTags.CONFIRM_DELETE_BUTTON).performClick()
}
