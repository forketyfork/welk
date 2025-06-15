package me.forketyfork.welk.service.auth

import co.touchlab.kermit.Logger
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.FirebaseAuthException
import dev.gitlive.firebase.auth.auth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext
import me.forketyfork.welk.Platform

/**
 * Implementation of the auth service based on Firestore authentication.
 */
class FirestoreAuthService(val platform: Platform) : AuthService {

    companion object {
        private val logger = Logger.withTag("FirestoreAuthService")
    }

    init {
        platform.initializeFirestore()
    }

    /**
     * Sign in with username (email) and password
     */
    override suspend fun signIn(username: String, password: String): String? {
        return try {
            val authResult = withContext(Dispatchers.IO) {
                Firebase.auth.signInWithEmailAndPassword(username, password)
            }
            if (authResult.user == null) {
                logger.e { "Sign-in failed: $authResult" }
            }
            authResult.user?.uid
        } catch (e: FirebaseAuthException) {
            logger.e(e) { "Sign-in failed: ${e.message}" }
            null
        }
    }

    /**
     * Sign out the current user
     */
    override suspend fun signOut() {
        try {
            withContext(Dispatchers.IO) {
                Firebase.auth.signOut()
            }
            logger.d { "Successfully signed out" }
        } catch (e: Exception) {
            logger.e(e) { "Sign-out failed: ${e.message}" }
        }
    }

    override fun currentUserId(): String? = Firebase.auth.currentUser?.uid
}
