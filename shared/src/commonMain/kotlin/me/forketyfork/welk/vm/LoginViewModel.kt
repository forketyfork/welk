package me.forketyfork.welk.vm

import kotlinx.coroutines.flow.StateFlow

/**
 * VM for the login screen.
 */
interface LoginViewModel : InitializableViewModel {
    /**
     * User ID or null if the user is not logged in.
     */
    val userId: StateFlow<String?>

    /**
     * True if the user failed to log in, false otherwise.
     */
    val loginError: StateFlow<Boolean>

    /**
     * Log in with username and password.
     */
    fun signIn(
        username: String,
        password: String,
    )

    /**
     * Sign out the current user.
     */
    fun signOut()
}
