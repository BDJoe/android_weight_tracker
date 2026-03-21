package com.josephlimbert.weighttracker.ui.auth

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.josephlimbert.weighttracker.data.model.ErrorMessage
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
    val user = authRepository.currentUser

    fun signInWithEmail(email: String, password: String, showErrorSnackbar: (String) -> Unit, navigateHome: () -> Unit) {
        viewModelScope.launch {
            when (val result = authRepository.signInWithEmail(email, password)) {
                is AuthResult.Success -> {
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

    fun signUpWithEmail(email: String, password: String, showErrorSnackbar: (String) -> Unit, navigateHome: () -> Unit) {
        viewModelScope.launch {
            when (val result = authRepository.signUpWithEmail(email, password)) {
                is AuthResult.Success -> {
                    firestoreRepository.createUserProfile(User(id = result.data.uid, email = email))
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

    fun linkAccount(email: String, password: String, showErrorSnackbar: (String) -> Unit, navigateHome: () -> Unit) {
        viewModelScope.launch {
            when (val result = authRepository.linkAccount(email, password)) {
                is AuthResult.Success -> {
                    firestoreRepository.linkUserProfile(result.data)
                    navigateHome()
                }
                is AuthResult.Error -> {
                    showErrorSnackbar(result.message)
                }
                is AuthResult.Loading -> {}
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