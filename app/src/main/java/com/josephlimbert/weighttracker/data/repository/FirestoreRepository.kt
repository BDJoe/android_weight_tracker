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

class FirestoreRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: AuthRepository
) {
    ///////////////////////////////////////
    // Weights
    ///////////////////////////////////////
    @OptIn(ExperimentalCoroutinesApi::class)
    val weightList: Flow<List<Weight>>
        get() = auth.currentUser.flatMapLatest { user ->
            if (user != null) {
                firestore
                    .collection(WEIGHT_ITEMS_COLLECTION)
                    .whereEqualTo(USER_ID_FIELD, user.uid)
                    .orderBy("recordedDate", Query.Direction.DESCENDING)
                    .dataObjects<Weight>()
            } else {
                emptyFlow()
            }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val goalWeight: Flow<Double>
        get() = auth.currentUser.flatMapLatest { user ->
            if (user != null) {
                firestore
                    .collection(USER_COLLECTION)
                    .document(user.uid)
                    .snapshots()
                    .map { document ->
                        val user = document.toObject<User>()
                        user?.goalWeight ?: 0.0
                    }
            } else {
                emptyFlow()
            }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val currentWeight: Flow<Weight>
        get() = auth.currentUser.flatMapLatest { user ->
            if (user != null) {
                firestore
                    .collection(WEIGHT_ITEMS_COLLECTION)
                    .whereEqualTo(USER_ID_FIELD, user.uid)
                    .orderBy("recordedDate", Query.Direction.DESCENDING)
                    .limit(1)
                    .dataObjects<Weight>()
                    .mapNotNull { snapshot ->
                        if (snapshot.isEmpty())
                            Weight()
                        else
                            snapshot.first()
                    }
            } else {
                emptyFlow()
            }
        }


    @OptIn(ExperimentalCoroutinesApi::class)
    val startingWeight: Flow<Weight>
        get() = auth.currentUser.flatMapLatest { user ->
            if (user != null) {
                firestore
                    .collection(WEIGHT_ITEMS_COLLECTION)
                    .whereEqualTo(USER_ID_FIELD, user.uid)
                    .orderBy("recordedDate", Query.Direction.ASCENDING)
                    .limit(1)
                    .dataObjects<Weight>()
                    .mapNotNull { snapshot ->
                        if (snapshot.isEmpty())
                            Weight()
                        else
                            snapshot.first()
                    }
            }
            else {
                emptyFlow()
            }
        }

    suspend fun  getWeight(weightId: String): Weight? {
        return firestore
                .collection(WEIGHT_ITEMS_COLLECTION)
                .document(weightId)
                .get()
                .await()
                .toObject<Weight>()
    }

    suspend fun addWeight(weight: Weight) {
        firestore
            .collection(WEIGHT_ITEMS_COLLECTION)
            .document(weight.id)
            .set(weight)
            .await()
    }

    suspend fun deleteWeight(weightId: String) {
        firestore
            .collection(WEIGHT_ITEMS_COLLECTION)
            .document(weightId)
            .delete()
            .await()
    }

    ///////////////////////////////////////
    // User Profile
    ///////////////////////////////////////

    @OptIn(ExperimentalCoroutinesApi::class)
    val weightUnit: Flow<String>
        get() = auth.currentUser.flatMapLatest { user ->
            if (user != null) {
                firestore
                    .collection(USER_COLLECTION)
                    .document(user.uid)
                    .snapshots()
                    .map { document ->
                        val user = document.toObject<User>()
                        user?.weightUnit ?: "lbs"
                    }
            } else {
                emptyFlow()
            }
        }

    @OptIn(ExperimentalCoroutinesApi::class)
    val userProfile: Flow<User?>
        get() = auth.currentUser.flatMapLatest { user ->
            if (user != null) {
                firestore
                    .collection(USER_COLLECTION)
                    .document(user.uid)
                    .snapshots()
                    .map { document ->
                        document.toObject<User>()
                    }
            } else {
                emptyFlow()
            }
        }

    suspend fun createUserProfile(user: User) {
        firestore
            .collection(USER_COLLECTION)
            .document(user.id)
            .set(user)
            .await()
    }

    suspend fun deleteUserProfile(userId: String) {
        firestore
            .collection(USER_COLLECTION)
            .document(userId)
            .delete()
            .await()
    }

    suspend fun linkUserProfile(user: FirebaseUser) {
        firestore
            .collection(USER_COLLECTION)
            .document(user.uid)
            .update("email", user.email)
            .await()
    }

    suspend fun setGoalWeight(userId: String, weight: Double) {
            firestore
                .collection(USER_COLLECTION)
                .document(userId)
                .update("goalWeight", weight)
                .await()
    }
    suspend fun setWeightUnit(userId: String, unit: String) {
        firestore
            .collection(USER_COLLECTION)
            .document(userId)
            .update("weightUnit", unit)
            .await()
    }


    companion object {
        private const val WEIGHT_ITEMS_COLLECTION = "weights"
        private const val USER_COLLECTION = "users"
        private const val USER_ID_FIELD = "userId"
    }
}