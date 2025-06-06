package me.forketyfork.welk

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.InternalComposeUiApi
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.*
import androidx.lifecycle.*
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import me.forketyfork.welk.components.LoginViewTestTags
import me.forketyfork.welk.components.SidePanelTestTags
import kotlin.test.fail

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
    val testUsername = System.getenv("WELK_TEST_USERNAME") ?: System.getProperty("WELK_TEST_USERNAME")
    val testPassword = System.getenv("WELK_TEST_PASSWORD") ?: System.getProperty("WELK_TEST_PASSWORD")

    if (testUsername.isNullOrBlank() || testPassword.isNullOrBlank()) {
        fail("WELK_TEST_USERNAME and WELK_TEST_PASSWORD must be set either as environment variables or in local.properties file")
    }

    return testUsername to testPassword
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
 * Logs in with the provided credentials
 */
@OptIn(ExperimentalTestApi::class)
fun ComposeUiTest.login(username: String, password: String) {
    onNodeWithTag(LoginViewTestTags.USERNAME_INPUT).performTextInput(username)
    onNodeWithTag(LoginViewTestTags.PASSWORD_INPUT).performTextInput(password)
    onNodeWithTag(LoginViewTestTags.SIGN_IN_BUTTON).performClick()
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
        .config.getOrNull(SemanticsProperties.TestTag) as? String
        ?: error("Deck tag not found for $name")
    return tag.removePrefix("deck_name_")
}
