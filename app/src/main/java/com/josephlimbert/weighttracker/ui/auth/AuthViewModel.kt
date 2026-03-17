package com.josephlimbert.weighttracker.ui.auth

import com.josephlimbert.weighttracker.MainViewModel
import com.josephlimbert.weighttracker.data.model.ErrorMessage
import com.josephlimbert.weighttracker.data.model.User
import com.josephlimbert.weighttracker.data.repository.AuthRepository
import com.josephlimbert.weighttracker.data.repository.FirestoreRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(private val firestoreRepository: FirestoreRepository, private val authRepository: AuthRepository): MainViewModel() {
    private val _shouldRestartApp = MutableStateFlow(false)
    val shouldRestartApp: StateFlow<Boolean>
        get() = _shouldRestartApp.asStateFlow()

    val userId = authRepository.currentUserIdFlow
    fun signInWithEmail(email: String, password: String, showErrorSnackbar: (ErrorMessage) -> Unit) {
        launchCatching(showErrorSnackbar) {
            authRepository.signInWithEmail(email, password)
            _shouldRestartApp.value = true
        }
    }

    fun signUpWithEmail(email: String, password: String, name: String, goalWeight: Double, weightUnit: String, showErrorSnackbar: (ErrorMessage) -> Unit) {
        launchCatching(showErrorSnackbar) {
            val userId = authRepository.signUpWithEmail(email, password)
            firestoreRepository.createUserProfile(User(id = userId!!, email = email, goalWeight = goalWeight, name = name, weightUnit = weightUnit))
            _shouldRestartApp.value = true
        }
    }

    fun createGuestAccount(name: String, goalWeight: Double, weightUnit: String, showErrorSnackbar: (ErrorMessage) -> Unit) {
        launchCatching {
            val userId = authRepository.createGuestAccount()
            firestoreRepository.createUserProfile(User(id = userId!!, email = "", goalWeight = goalWeight, name = name, weightUnit = weightUnit))
            _shouldRestartApp.value = true
        }
    }
}