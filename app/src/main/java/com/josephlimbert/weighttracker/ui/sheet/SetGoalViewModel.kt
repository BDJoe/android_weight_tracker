package com.josephlimbert.weighttracker.ui.sheet

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.josephlimbert.weighttracker.MainViewModel
import com.josephlimbert.weighttracker.data.model.Weight
import com.josephlimbert.weighttracker.data.repository.AuthRepository
import com.josephlimbert.weighttracker.data.repository.FirestoreRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SetGoalViewModel @Inject constructor(private val firestoreRepository: FirestoreRepository, authRepository: AuthRepository): MainViewModel() {
    val userId = authRepository.currentUserIdFlow
    val goalWeight = firestoreRepository.goalWeight

    fun setGoalWeight(userId: String, weight: Double) {
        launchCatching { firestoreRepository.setGoalWeight(userId, weight) }
    }
}