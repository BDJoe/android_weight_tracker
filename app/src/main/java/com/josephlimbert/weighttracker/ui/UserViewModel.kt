package com.josephlimbert.weighttracker.ui

import androidx.lifecycle.viewModelScope
import com.josephlimbert.weighttracker.MainViewModel
import com.josephlimbert.weighttracker.data.model.User
import com.josephlimbert.weighttracker.data.repository.AuthRepository
import com.josephlimbert.weighttracker.data.repository.WeightItemRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UserViewModel @Inject constructor(
    private val authRepository: AuthRepository,
) : MainViewModel() {
    private val _userProfile = MutableStateFlow<User?>(null)

    val userProfile: StateFlow<User?>
        get() = _userProfile.asStateFlow()

    val currentUserIdFlow: Flow<String?> = authRepository.currentUserIdFlow

    init {
        getUserProfile()
    }

    suspend fun createGuestAccount() {
        authRepository.createGuestAccount()
    }

    suspend fun signInWithEmail(email: String, password: String) {
        authRepository.signInWithEmail(email, password)
    }

    suspend fun signUpWithEmail(email: String, password: String) {
        authRepository.signUpWithEmail(email, password)
    }

    private fun getUserProfile() {
        viewModelScope.launch {
            authRepository.currentUserIdFlow.collect { userId ->
                if (userId != null) {
                    val user = authRepository.getUserProfile(userId)
                    if (user != null) {
                        _userProfile.value = user
                    }
                }
            }
        }
    }

    fun signOut() {
        authRepository.signOut()
    }

    suspend fun deleteAccount() {
        authRepository.deleteAccount()
    }
}