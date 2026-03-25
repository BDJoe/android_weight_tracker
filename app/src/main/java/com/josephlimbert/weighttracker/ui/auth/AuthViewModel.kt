package com.josephlimbert.weighttracker.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.josephlimbert.weighttracker.data.model.User
import com.josephlimbert.weighttracker.data.repository.AuthRepository
import com.josephlimbert.weighttracker.data.repository.AuthResult
import com.josephlimbert.weighttracker.data.repository.FirestoreRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(private val firestoreRepository: FirestoreRepository, private val authRepository: AuthRepository): ViewModel() {
    private val _isLoading = MutableStateFlow(false)
    val user = authRepository.currentUser
    val isLoading: StateFlow<Boolean>
        get() = _isLoading.asStateFlow()

    fun signInWithEmail(email: String, password: String, showErrorSnackbar: (String) -> Unit, navigateHome: () -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            when (val result = authRepository.signInWithEmail(email, password)) {
                is AuthResult.Success -> {
                    navigateHome()
                    _isLoading.value = false
                }
                is AuthResult.Error -> {
                    showErrorSnackbar(result.message)
                    _isLoading.value = false
                }
                is AuthResult.Loading -> {

                }
            }
        }
    }

    fun signUpWithEmail(email: String, password: String, showErrorSnackbar: (String) -> Unit, navigateHome: () -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            when (val result = authRepository.signUpWithEmail(email, password)) {
                is AuthResult.Success -> {
                    firestoreRepository.createUserProfile(User(id = result.data.uid, email = email))
                    navigateHome()
                    _isLoading.value = false
                }
                is AuthResult.Error -> {
                    showErrorSnackbar(result.message)
                    _isLoading.value = false
                }
                is AuthResult.Loading -> {

                }
            }
        }
    }

    fun linkAccount(email: String, password: String, showErrorSnackbar: (String) -> Unit, navigateHome: () -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            when (val result = authRepository.linkAccount(email, password)) {
                is AuthResult.Success -> {
                    firestoreRepository.linkUserProfile(result.data)
                    navigateHome()
                    _isLoading.value = false
                }
                is AuthResult.Error -> {
                    showErrorSnackbar(result.message)
                    _isLoading.value = false
                }
                is AuthResult.Loading -> {}
            }
        }
    }

    fun signInEmailAndDeleteGuest(email: String, password: String, guestId: String, showErrorSnackbar: (String) -> Unit, navigateHome: () -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            when (val result = authRepository.signInEmailAndDeleteGuest(email, password)) {
                is AuthResult.Success -> {
                    firestoreRepository.deleteUserProfile(guestId)
                    navigateHome()
                    _isLoading.value = false
                }
                is AuthResult.Error -> {
                    showErrorSnackbar(result.message)
                    _isLoading.value = false
                }
                is AuthResult.Loading -> {

                }
            }
        }
    }

    fun createGuestAccount(showErrorSnackbar: (String) -> Unit, navigateHome: () -> Unit) {
        viewModelScope.launch {
            when (val result = authRepository.createGuestAccount()) {
                is AuthResult.Success -> {
                    firestoreRepository.createUserProfile(User(id = result.data.uid))
                    navigateHome()
                }
                is AuthResult.Error -> {
                    showErrorSnackbar(result.message)
                }
                is AuthResult.Loading -> {

                }
            }
        }
    }
}