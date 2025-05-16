package me.forketyfork.welk.auth

/**
 * Authentication and authorization service
 */
interface AuthService {
    /**
     * Sign in with username and password
     * @return userId or null if authentication has failed
     */
    suspend fun signIn(username: String, password: String): String?
}