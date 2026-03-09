package com.josephlimbert.weighttracker.ui.home

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseUser
import com.josephlimbert.weighttracker.data.model.User
import com.josephlimbert.weighttracker.data.model.Weight
import com.josephlimbert.weighttracker.data.repository.AuthRepository
import com.josephlimbert.weighttracker.data.repository.AuthResult
import com.josephlimbert.weighttracker.data.repository.FirestoreRepository
import com.josephlimbert.weighttracker.data.repository.FirestoreResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class UiState(
    val isLoading: Boolean = false,
    val profile: User? = null,
    val currentWeight: Weight? = null,
    val startingWeight: Weight? = null,
    val goalWeight: Double? = null,
    val totalLossPercent: Double = 0.0,
    val totalLossWeight: Double = 0.0,
    val targetLoss: Double = 0.0,
    val targetLeft: Double = 0.0,
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val firestoreRepository: FirestoreRepository,
    private val authRepository: AuthRepository
): ViewModel() {
    private val _uiState = MutableStateFlow(UiState())
    private val _goalWeight = MutableStateFlow<Double?>(null)
    val currentWeight = firestoreRepository.currentWeight
    val startingWeight = firestoreRepository.startingWeight
    val goalWeight: StateFlow<Double?>
        get() =_goalWeight.asStateFlow()
    val auth = authRepository.authStateFlow
    val userProfile = firestoreRepository.userProfile

    val totalLossPercent: StateFlow<Double> =
        combine(startingWeight, currentWeight, _goalWeight) { starting, current, goal ->
            val result = if (starting != null && current != null && goal != null) {
                (((starting.weight - current.weight) / (starting.weight - goal)) * 100.0).coerceAtMost(
                    100.0
                )
            } else { 0.0 }
            return@combine result
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = 0.0
        )

    val totalLossWeight: StateFlow<Double> =
        combine(startingWeight, currentWeight) { starting, current ->
            val result = if (starting != null && current != null) {
                starting.weight - current.weight
            } else { 0.0 }
            return@combine result
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = 0.0
        )

    val targetLoss: StateFlow<Double> =
        combine(startingWeight, _goalWeight) { starting, goal ->
            val result = if (starting != null && goal != null) {
                starting.weight - goal
            } else { 0.0 }
            return@combine result
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = 0.0
        )

    val targetLeft: StateFlow<Double> =
        combine(startingWeight, currentWeight, _goalWeight) { starting, current, goal ->
            val result = if (starting != null && current != null && goal != null) {
                (starting.weight - goal) - (starting.weight - current.weight)
            } else { 0.0 }
            return@combine result
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = 0.0
        )

    init {
        viewModelScope.launch {
            firestoreRepository.userProfile.collect { user ->
                _goalWeight.value = user?.goalWeight
            }
        }
    }

    suspend fun createGuestAccount(): AuthResult<FirebaseUser> {
        return authRepository.createGuestAccount()
    }

    suspend fun createUserProfile(user: FirebaseUser): FirestoreResult<Unit> {
        return firestoreRepository.createUserProfile(user)
    }
//    val uiState: StateFlow<UiState>
//        get() = _uiState.asStateFlow()
//
//    init {
//        viewModelScope.launch {
//            authRepository.authStateFlow.collect { user ->
//                if (user == null) {
//                    _uiState.value = _uiState.value.copy(isLoading = true)
//                } else {
//                    viewModelScope.launch {
//                        firestoreRepository.getWeightList(user.uid).collect { weights ->
//                            if (!weights.isEmpty()) {
//                                _startingWeight.value = weights.last()
//                                _currentWeight.value = weights.first()
//                                _uiState.value = _uiState.value.copy(
//                                    startingWeight = weights.last(),
//                                    currentWeight = weights.first(),
//                                )
//                            }
//                        }
//                    }
//                    viewModelScope.launch {
//                        firestoreRepository.userProfileFlow(user.uid).collect { profile ->
//                            if (profile != null) {
//                                _goalWeight.value = profile.goalWeight
//                                _uiState.value = _uiState.value.copy(
//                                    goalWeight = profile.goalWeight,
//                                    profile = profile,
//                                    isLoading = false
//                                )
//                            }
//                        }
//                    }
//                    viewModelScope.launch {
//                        totalLossPercent.collect { result ->
//                            _uiState.value = _uiState.value.copy(totalLossPercent = result)
//                        }
//                    }
//                    viewModelScope.launch {
//                        totalLossWeight.collect { result ->
//                            Log.d("HOME", result.toString())
//                            _uiState.value = _uiState.value.copy(totalLossWeight = result)
//                        }
//                    }
//                    viewModelScope.launch {
//                        targetLoss.collect { result ->
//                            _uiState.value = _uiState.value.copy(targetLoss = result)
//                        }
//                    }
//                    viewModelScope.launch {
//                        targetLeft.collect { result ->
//                            _uiState.value = _uiState.value.copy(targetLeft = result)
//                        }
//                    }
//                }
//            }
//        }
//    }
}