package com.josephlimbert.weighttracker.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.josephlimbert.weighttracker.data.repository.AuthRepository
import com.josephlimbert.weighttracker.data.repository.FirestoreRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val authRepository: AuthRepository, private val firestoreRepository: FirestoreRepository
) : ViewModel() {
    val user = authRepository.currentUser
    val weightUnit = firestoreRepository.weightUnit
    val userProfile = firestoreRepository.userProfile

    fun changeWeightUnit(userId: String, weightUnit: String) {
        viewModelScope.launch {
            firestoreRepository.setWeightUnit(userId, weightUnit)
        }
    }

    fun deleteUserProfile(userId: String) {
        viewModelScope.launch {
            firestoreRepository.deleteUserProfile(userId)
        }
    }

    fun signOut() {
        authRepository.signOut()
    }
}