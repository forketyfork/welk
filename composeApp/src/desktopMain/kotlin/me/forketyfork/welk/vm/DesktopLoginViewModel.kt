package me.forketyfork.welk.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import me.forketyfork.welk.service.auth.AuthService

/**
 * Implementation of the login view model for the desktop app.
 */
class DesktopLoginViewModel(authService: AuthService) : ViewModel(),
    LoginViewModel by SharedLoginViewModel(authService = authService) {

    init {
        initialize(viewModelScope)
    }

}