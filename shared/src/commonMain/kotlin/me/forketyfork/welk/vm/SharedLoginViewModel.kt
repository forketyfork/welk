package me.forketyfork.welk.vm

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import me.forketyfork.welk.service.auth.AuthService

/**
 * Implementation of the LoginViewModel shared across Desktop and Mobile.
 */
class SharedLoginViewModel(
    private val authService: AuthService,
) : LoginViewModel {

    private lateinit var viewModelScope: CoroutineScope

    private val _userId = MutableStateFlow(null as String?)
    override val userId: StateFlow<String?> = _userId.asStateFlow()

    private val _loginError = MutableStateFlow(false)
    override val loginError: StateFlow<Boolean> = _loginError.asStateFlow()

    override fun signIn(username: String, password: String) {
        viewModelScope.launch {
            val userId = authService.signIn(username, password)
            _userId.value = userId
            _loginError.value = userId == null
        }
    }

    override fun signOut() {
        viewModelScope.launch {
            authService.signOut()
            _userId.value = null
        }
    }

    override fun initialize(viewModelScope: CoroutineScope) {
        this.viewModelScope = viewModelScope
    }

}