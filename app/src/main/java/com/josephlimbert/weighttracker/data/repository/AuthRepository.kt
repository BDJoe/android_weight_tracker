package com.josephlimbert.weighttracker.data.repository

import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.toObject
import com.josephlimbert.weighttracker.data.model.User
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

// Sealed class to represent different authentication states
sealed class AuthResult<out T> {
    data class Success<T>(val data: T) : AuthResult<T>()
    data class Error(val message: String, val ex: Exception? = null) : AuthResult<Nothing>()
}

class AuthRepository @Inject constructor(
    private val auth: FirebaseAuth
) {
    val isAuthenticated: Boolean
        get() = auth.currentUser != null

    val authStateFlow: Flow<FirebaseUser?>
        get() = callbackFlow {
            val listener = FirebaseAuth.AuthStateListener { auth -> this.trySend(auth.currentUser) }
            auth.addAuthStateListener(listener)
            awaitClose { auth.removeAuthStateListener(listener) }
        }

    suspend fun createGuestAccount(): AuthResult<FirebaseUser> {
        return try {
            val result = auth.signInAnonymously().await()
            result.user?.let {
                AuthResult.Success(it)
            } ?: AuthResult.Error("User creation failed")
        } catch (e: Exception) {
            AuthResult.Error(e.message ?: "Unknown error occurred")
        }
    }

    suspend fun signInWithEmail(email: String, password: String): AuthResult<FirebaseUser> {
        return try {
            val result = auth.signInWithEmailAndPassword(email, password).await()
            result.user?.let {
                AuthResult.Success(it)
            } ?: AuthResult.Error("User sign in failed")
        } catch (e: Exception) {
            AuthResult.Error(e.message ?: "Unknown error occurred", e)
        }
    }

    suspend fun linkAccount(email: String, password: String): AuthResult<FirebaseUser> {
        return try {
            val credential = EmailAuthProvider.getCredential(email, password)
            val result = auth.currentUser!!.linkWithCredential(credential).await()
            result.user?.let {
                AuthResult.Success(it)
            } ?: AuthResult.Error("Account linking failed")
        } catch(e: Exception) {
            AuthResult.Error(e.message ?: "Unknown error occurred", e)
        }
    }

    fun signOut() {
        if (auth.currentUser!!.isAnonymous) {
            auth.currentUser!!.delete()
        }
        auth.signOut()
    }

    suspend fun deleteAccount(): AuthResult<Unit> {
        return try {
            auth.currentUser?.delete()?.await()
            AuthResult.Success(Unit)
        } catch (e: Exception) {
            AuthResult.Error(e.message ?: "Failed to delete account")
        }
    }
}