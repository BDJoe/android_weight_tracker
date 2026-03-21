package com.josephlimbert.weighttracker.ui.sheet

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.josephlimbert.weighttracker.data.repository.AuthRepository
import com.josephlimbert.weighttracker.data.repository.FirestoreRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SetGoalViewModel @Inject constructor(private val firestoreRepository: FirestoreRepository, authRepository: AuthRepository): ViewModel() {
    val user = authRepository.currentUser
    val goalWeight = firestoreRepository.goalWeight

    fun setGoalWeight(userId: String, weight: Double) {
        viewModelScope.launch { firestoreRepository.setGoalWeight(userId, weight) }
    }
}