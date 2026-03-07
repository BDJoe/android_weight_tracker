package com.josephlimbert.weighttracker.data.datasource

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.dataObjects
import com.google.firebase.firestore.toObject
import com.josephlimbert.weighttracker.data.model.Weight
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class WeightItemRemoteDataSource @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    @OptIn(ExperimentalCoroutinesApi::class)
    fun getWeightList(currentUserIdFlow: Flow<String?>): Flow<List<Weight>> {
        return currentUserIdFlow.flatMapLatest { userId ->
            firestore
                .collection(WEIGHT_ITEMS_COLLECTION)
                .whereEqualTo(USER_ID_FIELD, userId)
                .orderBy("recordedDate", Query.Direction.DESCENDING)
                .dataObjects()
        }
    }
    suspend fun  getWeight(weightId: String): Weight? {
        return firestore
            .collection(WEIGHT_ITEMS_COLLECTION)
            .document(weightId)
            .get()
            .await()
            .toObject()
    }

    suspend fun addWeight(weight: Weight): String {
        return firestore
            .collection(WEIGHT_ITEMS_COLLECTION)
            .add(weight)
            .await()
            .id
    }

    suspend fun updateWeight(weight: Weight) {
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

    companion object {
        private const val WEIGHT_ITEMS_COLLECTION = "weights"
        private const val USER_ID_FIELD = "userId"
    }
}