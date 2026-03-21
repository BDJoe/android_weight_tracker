package com.josephlimbert.weighttracker.data.repository

import androidx.compose.runtime.LaunchedEffect
import androidx.credentials.exceptions.CreateCredentialUnknownException
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.toObject
import com.josephlimbert.weighttracker.data.model.ErrorMessage
import com.josephlimbert.weighttracker.data.model.User
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

sealed class AuthResult<out T> {
    data class Success<T>(val data: T) : AuthResult<T>()
    data class Error(val error: Exception? = null, val message: String) : AuthResult<Nothing>()
    data object Loading : AuthResult<Nothing>()
}

class AuthRepository @Inject constructor(
    private val auth: FirebaseAuth
) {
    val currentUser: Flow<FirebaseUser?>
        get() = callbackFlow {
            val listener = FirebaseAuth.AuthStateListener { auth ->
                this.trySend(auth.currentUser)
            }
            auth.addAuthStateListener(listener)
            awaitClose { auth.removeAuthStateListener(listener) }
        }

    suspend fun createGuestAccount() : AuthResult<FirebaseUser> {
        return try {
            val result = auth.signInAnonymously().await()
            result.user?.let {
                AuthResult.Success(it)
            } ?: AuthResult.Error(message = "Sign In Failed")
        } catch (e: Exception) {
            AuthResult.Error(e, e.message ?: "Unknown Error Occurred")
        }
    }

    suspend fun signInWithEmail(email: String, password: String) : AuthResult<FirebaseUser> {
        return try {
            val result = auth.signInWithEmailAndPassword(email, password).await()
            result.user?.let {
                AuthResult.Success(it)
            } ?: AuthResult.Error(message = "Sign In Failed")
        } catch (e: Exception) {
            AuthResult.Error(e, e.message ?: "Unknown Error Occurred")
        }
    }

    suspend fun signUpWithEmail(email: String, password: String): AuthResult<FirebaseUser> {
        return try {
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            result.user?.let {
                AuthResult.Success(it)
            } ?: AuthResult.Error(message = "Register Account Failed")
        } catch (e: Exception) {
            AuthResult.Error(e, e.message ?: "Unknown Error Occurred")
        }
    }

    suspend fun linkAccount(email: String, password: String): AuthResult<FirebaseUser> {
        val credential = EmailAuthProvider.getCredential(email, password)
        return try {
            val result = auth.currentUser!!.linkWithCredential(credential).await()
            result.user?.let {
                AuthResult.Success(it)
            } ?: AuthResult.Error(message = "Account Linking Failed")
        } catch (e: Exception) {
            AuthResult.Error(e, e.message ?: "Unknown Error Occurred")
        }
    }

    suspend fun changePassword(oldPass: String, newPass: String): AuthResult<Unit> {
        return try {
            val user = auth.currentUser
            val credential = EmailAuthProvider.getCredential(user!!.email!!, oldPass)
            user.reauthenticate(credential).await()
            user.updatePassword(newPass).await()
            AuthResult.Success(Unit)
        } catch (e: Exception) {
            AuthResult.Error(e, e.message ?: "Unknown Error Occurred")
        }
    }

    fun signOut() {
        if (auth.currentUser!!.isAnonymous) {
            auth.currentUser!!.delete()
        }
        auth.signOut()
    }

    suspend fun deleteAccount() {
        auth.currentUser?.delete()?.await()
    }
}