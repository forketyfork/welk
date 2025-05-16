package me.forketyfork.welk.vm

import androidx.lifecycle.ViewModel

/**
 * Implementation of the login view model for the desktop app.
 */
class DesktopLoginViewModel : ViewModel(), LoginViewModel by SharedLoginViewModel()