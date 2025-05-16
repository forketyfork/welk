package me.forketyfork.welk.auth

import co.touchlab.kermit.Logger
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.FirebaseAuthException
import dev.gitlive.firebase.auth.auth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext

/**
 * Implementation of the auth service based on Firestore authentication.
 */
class FirestoreAuthService : AuthService {

    private val logger = Logger.withTag("FirestoreAuthService")

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
}
