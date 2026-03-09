package com.josephlimbert.weighttracker.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.dataObjects
import com.google.firebase.firestore.snapshots
import com.google.firebase.firestore.toObject
import com.josephlimbert.weighttracker.R
import com.josephlimbert.weighttracker.data.model.User
import com.josephlimbert.weighttracker.data.model.Weight
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import kotlin.text.get
import kotlin.text.set

sealed class FirestoreResult<out T> {
    data class Success<T>(val data: T) : FirestoreResult<T>()
    data class Error(val message: String) : FirestoreResult<Nothing>()
}
class FirestoreRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: AuthRepository
) {
    ///////////////////////////////////////
    // Weights
    ///////////////////////////////////////
    @OptIn(ExperimentalCoroutinesApi::class)
    val weightList: Flow<List<Weight>>
        get() = auth.authStateFlow.flatMapLatest { user ->
        firestore
            .collection(WEIGHT_ITEMS_COLLECTION)
            .whereEqualTo(USER_ID_FIELD, user?.uid)
            .orderBy("recordedDate", Query.Direction.DESCENDING)
            .dataObjects<Weight>()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val userProfile: Flow<User?>
        get() = auth.authStateFlow.flatMapLatest { user ->
            if (user != null) {
                firestore
                    .collection(USER_COLLECTION)
                    .document(user.uid ?: "")
                    .snapshots()
                    .map { document ->
                        document.toObject()
                    }
            } else {
                emptyFlow<User>()
            }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val currentWeight: Flow<Weight?>
        get() = auth.authStateFlow.flatMapLatest { user ->
            if (user != null) {
                firestore
                    .collection(WEIGHT_ITEMS_COLLECTION)
                    .whereEqualTo(USER_ID_FIELD, user.uid)
                    .orderBy("recordedDate", Query.Direction.DESCENDING)
                    .limit(1)
                    .dataObjects<Weight>()
                    .mapNotNull { snapshot ->
                        snapshot.firstOrNull()
                    }
            } else {
                emptyFlow()
            }
        }


    @OptIn(ExperimentalCoroutinesApi::class)
    val startingWeight: Flow<Weight?>
        get() = auth.authStateFlow.flatMapLatest { user ->
            if (user != null) {
                firestore
                    .collection(WEIGHT_ITEMS_COLLECTION)
                    .whereEqualTo(USER_ID_FIELD, user.uid)
                    .orderBy("recordedDate", Query.Direction.ASCENDING)
                    .limit(1)
                    .dataObjects<Weight>()
                    .mapNotNull { snapshot ->
                        snapshot.firstOrNull()
                    }
            }
            else {
                emptyFlow()
            }
        }

    suspend fun  getWeight(weightId: String): FirestoreResult<Weight?> {
        return try {
            val weight = firestore
                .collection(WEIGHT_ITEMS_COLLECTION)
                .document(weightId)
                .get()
                .await()
                .toObject<Weight>()
            FirestoreResult.Success(weight)
        } catch (e: Exception) {
            FirestoreResult.Error(e.message ?: "An unknown error has occurred")
        }
    }

    suspend fun addWeight(weight: Weight): FirestoreResult<Unit> {
        return try {
            firestore
                .collection(WEIGHT_ITEMS_COLLECTION)
                .document(weight.id)
                .set(weight)
                .await()
            FirestoreResult.Success(Unit)
        } catch (e: Exception) {
            FirestoreResult.Error(e.message ?: "An unknown error has occurred")
        }
    }

    suspend fun deleteWeight(weightId: String): FirestoreResult<Unit> {
        return try {
            firestore
                .collection(WEIGHT_ITEMS_COLLECTION)
                .document(weightId)
                .delete()
                .await()
            FirestoreResult.Success(Unit)
        } catch (e: Exception) {
            FirestoreResult.Error(e.message ?: "An unknown error has occurred")
        }
    }

    ///////////////////////////////////////
    // User Profile
    ///////////////////////////////////////

    suspend fun createUserProfile(user: FirebaseUser): FirestoreResult<Unit> {
        return try {
            val newUser =
                User(id = user.uid, email = user.email ?: "")
            firestore
                .collection(USER_COLLECTION)
                .document(newUser.id)
                .set(newUser)
                .await()
            FirestoreResult.Success(Unit)
        } catch (e: Exception) {
            FirestoreResult.Error(e.message ?: "An unknown error has occurred")
        }
    }

    suspend fun linkUserProfile(user: FirebaseUser): FirestoreResult<Unit> {
        return try {
            firestore
                .collection(USER_COLLECTION)
                .document(user.uid)
                .update("email", user.email)
                .await()
            FirestoreResult.Success(Unit)
        } catch (e: Exception) {
            FirestoreResult.Error(e.message ?: "An unknown error has occurred")
        }
    }

    suspend fun setGoalWeight(userId: String, weight: Double): FirestoreResult<Unit> {
            return try {
                firestore
                    .collection(USER_COLLECTION)
                    .document(userId)
                    .update("goalWeight", weight)
                    .await()
                FirestoreResult.Success(Unit)
            } catch (e: Exception) {
                FirestoreResult.Error(e.message ?: "An unknown error has occurred")
            }
    }

    companion object {
        private const val WEIGHT_ITEMS_COLLECTION = "weights"
        private const val USER_COLLECTION = "users"
        private const val USER_ID_FIELD = "userId"
    }
}