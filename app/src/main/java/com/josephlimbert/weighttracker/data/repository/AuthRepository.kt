package com.josephlimbert.weighttracker.data.repository

import androidx.compose.runtime.LaunchedEffect
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

class AuthRepository @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestoreRepository: FirestoreRepository
) {
    val currentUser: FirebaseUser?
        get() = auth.currentUser

    val currentUserIdFlow: Flow<String?>
        get() = callbackFlow {
            val listener = FirebaseAuth.AuthStateListener { auth -> this.trySend(auth.currentUser?.uid) }
            auth.addAuthStateListener(listener)
            awaitClose { auth.removeAuthStateListener(listener) }
        }

    suspend fun createGuestAccount(): String? {
        return auth.signInAnonymously().await().user?.uid
    }

    suspend fun signInWithEmail(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password).await()
    }

    suspend fun signUpWithEmail(email: String, password: String): String? {
        val userId = auth.createUserWithEmailAndPassword(email, password).await().user?.uid
        return userId
    }

    suspend fun linkAccount(email: String, password: String): FirebaseUser? {
        val credential = EmailAuthProvider.getCredential(email, password)
        return auth.currentUser!!.linkWithCredential(credential).await().user
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