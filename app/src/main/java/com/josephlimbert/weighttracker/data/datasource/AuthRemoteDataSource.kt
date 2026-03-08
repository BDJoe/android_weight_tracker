package com.josephlimbert.weighttracker.data.datasource

import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.toObject
import com.josephlimbert.weighttracker.data.model.User
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import com.google.firebase.firestore.toObject
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class AuthRemoteDataSource @Inject constructor(
    private val auth: FirebaseAuth, private val firestore: FirebaseFirestore
) {
    val currentUser: FirebaseUser? get() = auth.currentUser

    val currentUserIdFlow: Flow<String?>
        get() = callbackFlow {
            val listener = FirebaseAuth.AuthStateListener { _ -> this.trySend(currentUser?.uid) }
            auth.addAuthStateListener(listener)
            awaitClose { auth.removeAuthStateListener(listener) }
        }

    suspend fun createGuestAccount() {
        val user = auth.signInAnonymously().await().user
        createUserAccount(user!!)
    }

    suspend fun signInWithEmail(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password).await()
    }

    suspend fun linkAccount(email: String, password: String) {
        val credential = EmailAuthProvider.getCredential(email, password)
        val user = auth.currentUser!!.linkWithCredential(credential).await().user
        createUserAccount(user!!)
    }

    private suspend fun createUserAccount(user: FirebaseUser) {
        val newUser = User(id = user.uid, email = user.email ?: "", isAnonymous = user.isAnonymous)
        firestore
            .collection(USER_COLLECTION)
            .document(newUser.id)
            .set(newUser)
            .await()
    }

    suspend fun getUserProfile(userId: String): User? {
        return firestore
                .collection(USER_COLLECTION)
                .document(userId)
                .get()
                .await()
                .toObject()
    }

    suspend fun setGoalWeight(weight: Double) {
        currentUser?.uid?.let {
            firestore
                .collection(USER_COLLECTION)
                .document(it)
                .update("goalWeight", weight)
                .await()
        }
    }

    fun signOut(){
        if (auth.currentUser!!.isAnonymous) {
            auth.currentUser!!.delete()
        }
        auth.signOut()
    }

    suspend fun deleteAccount() {
        auth.currentUser!!.delete().await()
    }

    companion object {
        private const val USER_COLLECTION = "users"
    }
}