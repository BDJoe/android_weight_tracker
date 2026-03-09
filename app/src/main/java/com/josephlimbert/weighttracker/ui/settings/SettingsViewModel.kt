package com.josephlimbert.weighttracker.ui.settings

import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseUser
import com.josephlimbert.weighttracker.data.repository.AuthRepository
import com.josephlimbert.weighttracker.data.repository.AuthResult
import com.josephlimbert.weighttracker.data.model.User
import com.josephlimbert.weighttracker.data.repository.FirestoreRepository
import com.josephlimbert.weighttracker.data.repository.FirestoreResult
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

data class AuthUiState(
    val isLoading: Boolean = false,
    val user: FirebaseUser? = null,
    val profile: User? = null,
    val errorMessage: String? = null,
    val isAuthenticated: Boolean = false
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val authRepository: AuthRepository, private val firestoreRepository: FirestoreRepository
) : ViewModel() {
    val user = authRepository.authStateFlow

    fun signOut() {
        authRepository.signOut()
    }
}