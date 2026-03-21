package com.josephlimbert.weighttracker.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.josephlimbert.weighttracker.data.repository.AuthRepository
import com.josephlimbert.weighttracker.data.repository.FirestoreRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val firestoreRepository: FirestoreRepository,
    private val authRepository: AuthRepository
): ViewModel() {
    private val _isLoadingUser = MutableStateFlow(true)
    val user = authRepository.currentUser
    val isLoadingUser: StateFlow<Boolean>
        get() = _isLoadingUser.asStateFlow()
    val currentWeight = firestoreRepository.currentWeight
    val startingWeight = firestoreRepository.startingWeight
    val goalWeight = firestoreRepository.goalWeight
    val weightUnit = firestoreRepository.weightUnit

    val totalLossPercent: StateFlow<Double> =
        combine(startingWeight, currentWeight, goalWeight) { starting, current, goal ->
            (((starting.weight - current.weight) / (starting.weight - goal)) * 100.0).coerceIn(0.0, 100.0)
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = 0.0
        )

    val totalLossWeight: StateFlow<Double> =
        combine(startingWeight, currentWeight) { starting, current ->
            (starting.weight - current.weight).coerceAtLeast(0.0)
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = 0.0
        )

    val targetLoss: StateFlow<Double> =
        combine(startingWeight, goalWeight) { starting, goal ->
                (starting.weight - goal).coerceAtLeast(0.0)
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = 0.0
        )

    val targetLeft: StateFlow<Double> =
        combine(startingWeight, currentWeight, goalWeight) { starting, current, goal ->
            ((starting.weight - goal) - (starting.weight - current.weight)).coerceAtLeast(0.0)
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = 0.0
        )
}