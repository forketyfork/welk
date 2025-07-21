package me.forketyfork.welk.fake

import me.forketyfork.welk.service.auth.AuthService

class FakeAuthService : AuthService {
    private var signedIn = false
    override suspend fun signIn(username: String, password: String): String? {
        signedIn = true
        return if (username.isNotEmpty()) "test-user" else null
    }

    override suspend fun signOut() {
        signedIn = false
    }
}
