package com.josephlimbert.weighttracker.ui.home

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseUser
import com.josephlimbert.weighttracker.MainViewModel
import com.josephlimbert.weighttracker.data.model.User
import com.josephlimbert.weighttracker.data.model.Weight
import com.josephlimbert.weighttracker.data.repository.AuthRepository
import com.josephlimbert.weighttracker.data.repository.FirestoreRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val firestoreRepository: FirestoreRepository,
    private val authRepository: AuthRepository
): MainViewModel() {
    private val _isLoadingUser = MutableStateFlow(true)
    val userId = authRepository.currentUserIdFlow
    val isLoadingUser: StateFlow<Boolean>
        get() = _isLoadingUser.asStateFlow()
    val currentWeight = firestoreRepository.currentWeight
    val startingWeight = firestoreRepository.startingWeight
    val goalWeight = firestoreRepository.goalWeight

    val totalLossPercent: StateFlow<Double> =
        combine(startingWeight, currentWeight, goalWeight) { starting, current, goal ->
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
        combine(startingWeight, goalWeight) { starting, goal ->
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
        combine(startingWeight, currentWeight, goalWeight) { starting, current, goal ->
            val result = if (starting != null && current != null && goal != null) {
                (starting.weight - goal) - (starting.weight - current.weight)
            } else { 0.0 }
            return@combine result
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = 0.0
        )

    fun loadCurrentUser() {
        launchCatching {
            if (authRepository.currentUser == null) {
                val user = authRepository.createGuestAccount()
                firestoreRepository.createUserProfile(user!!)
            }

            _isLoadingUser.value = false
        }
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