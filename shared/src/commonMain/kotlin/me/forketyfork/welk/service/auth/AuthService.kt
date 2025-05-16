package me.forketyfork.welk.service.auth

/**
 * Authentication and authorization service
 */
interface AuthService {
    /**
     * Sign in with username and password
     * @return userId or null if authentication has failed
     */
    suspend fun signIn(username: String, password: String): String?
    
    /**
     * Sign out the current user
     */
    suspend fun signOut()
}