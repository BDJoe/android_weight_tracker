package com.josephlimbert.weighttracker.ui.sheet

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.josephlimbert.weighttracker.data.model.Weight
import com.josephlimbert.weighttracker.data.repository.AuthRepository
import com.josephlimbert.weighttracker.data.repository.FirestoreRepository
import com.josephlimbert.weighttracker.data.repository.FirestoreResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SetGoalViewModel @Inject constructor(private val firestoreRepository: FirestoreRepository, private val authRepository: AuthRepository): ViewModel() {
    val user = firestoreRepository.userProfile

    suspend fun setGoalWeight(userId: String, weight: Double): FirestoreResult<Unit> {
        return firestoreRepository.setGoalWeight(userId, weight)
    }
}