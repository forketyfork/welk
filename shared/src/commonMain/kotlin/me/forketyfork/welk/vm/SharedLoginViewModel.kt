package me.forketyfork.welk.vm

import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import me.forketyfork.welk.auth.AuthService
import me.forketyfork.welk.auth.FirestoreAuthService

/**
 * Implementation of the LoginViewModel shared across Desktop and Mobile.
 */
class SharedLoginViewModel(
    private val authService: AuthService = FirestoreAuthService(),
) : LoginViewModel {

    // TODO use the model scope instead of the main scope
    private val mainScope = MainScope()

    private val _userId = MutableStateFlow(null as String?)
    override val userId: StateFlow<String?> = _userId.asStateFlow()

    private val _loginError = MutableStateFlow(false)
    override val loginError: StateFlow<Boolean> = _loginError.asStateFlow()

    override fun signIn(username: String, password: String) {
        mainScope.launch {
            val userId = authService.signIn(username, password)
            _userId.value = userId
            _loginError.value = userId == null
        }
    }

}