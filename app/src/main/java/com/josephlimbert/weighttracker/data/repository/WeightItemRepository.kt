package com.josephlimbert.weighttracker.data.repository

import com.josephlimbert.weighttracker.data.datasource.WeightItemRemoteDataSource
import com.josephlimbert.weighttracker.data.model.Weight
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class WeightItemRepository @Inject constructor(
    private val weightItemRemoteDataSource: WeightItemRemoteDataSource
) {
    fun getWeightList(currentUserIdFlow: Flow<String?>): Flow<List<Weight>> {
        return weightItemRemoteDataSource.getWeightList(currentUserIdFlow)
    }

    suspend fun getWeight(weightId: String): Weight? {
        return weightItemRemoteDataSource.getWeight(weightId)
    }

    suspend fun addWeight(weight: Weight): String {
        return weightItemRemoteDataSource.addWeight(weight)
    }

    suspend fun deleteWeight(weightId: String) {
        weightItemRemoteDataSource.deleteWeight(weightId)
    }
}