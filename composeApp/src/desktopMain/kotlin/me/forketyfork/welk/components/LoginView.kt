package me.forketyfork.welk.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import me.forketyfork.welk.vm.DesktopLoginViewModel
import me.forketyfork.welk.vm.LoginViewModel
import org.koin.compose.viewmodel.koinViewModel

/**
 * Login screen with username and password input.
 */
@Composable
fun LoginView() {

    val viewModel: LoginViewModel = koinViewModel<DesktopLoginViewModel>()

    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val usernameFocusRequester = remember { FocusRequester() }
    val loginError = viewModel.loginError.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        usernameFocusRequester.requestFocus()
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Welkome\uD83C\uDF42",
            style = MaterialTheme.typography.h1,
            color = MaterialTheme.colors.primary,
            textAlign = TextAlign.Center
        )
        OutlinedTextField(
            value = username,
            colors = TextFieldDefaults.textFieldColors(
                textColor = MaterialTheme.colors.onSurface
            ),
            onValueChange = { username = it },
            label = { Text("Username") },
            singleLine = true,
            modifier = Modifier.width(300.dp)
                .focusRequester(usernameFocusRequester)
                .testTag(LoginViewTestTags.USERNAME_INPUT),
        )
        OutlinedTextField(
            value = password,
            colors = TextFieldDefaults.textFieldColors(
                textColor = MaterialTheme.colors.onSurface
            ),
            onValueChange = { password = it },
            label = { Text("Password") },
            singleLine = true,
            modifier = Modifier.width(300.dp)
                .testTag(LoginViewTestTags.PASSWORD_INPUT),
            visualTransformation = PasswordVisualTransformation()
        )
        TextButton(
            onClick = {
                viewModel.signIn(username, password)
            },
            modifier = Modifier.testTag(LoginViewTestTags.SIGN_IN_BUTTON)
        ) {
            Text("Sign in")
        }
        Text(
            text = if (loginError.value) "Invalid username or password" else "",
            color = MaterialTheme.colors.error,
            modifier = Modifier.padding(vertical = 16.dp)
        )
    }
}

object LoginViewTestTags {
    const val USERNAME_INPUT = "login_username_input"
    const val PASSWORD_INPUT = "login_password_input"
    const val SIGN_IN_BUTTON = "login_sign_in_button"
}
