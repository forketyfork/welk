package me.forketyfork.welk

import androidx.lifecycle.ViewModel
import me.forketyfork.welk.vm.LoginViewModel
import me.forketyfork.welk.vm.SharedLoginViewModel

/**
 * Implementation of the login view model for the desktop app.
 */
class DesktopLoginViewModel : ViewModel(), LoginViewModel by SharedLoginViewModel()