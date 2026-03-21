package com.josephlimbert.weighttracker.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.josephlimbert.weighttracker.data.repository.AuthRepository
import com.josephlimbert.weighttracker.data.repository.FirestoreRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HistoryViewModel @Inject constructor(private val firestoreRepository: FirestoreRepository, authRepository: AuthRepository) : ViewModel() {
    val weights = firestoreRepository.weightList
    val goalWeight = firestoreRepository.goalWeight
    val user = authRepository.currentUser
    val weightUnit = firestoreRepository.weightUnit

    fun deleteWeight(weightId: String) {
        viewModelScope.launch {
            firestoreRepository.deleteWeight(weightId)
        }
    }

    fun getWeightDiff(weight: Double, prevWeight: Double): Double {
        return weight - prevWeight
    }
}