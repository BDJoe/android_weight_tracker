package com.josephlimbert.weighttracker.ui.signin

import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseUser
import com.josephlimbert.weighttracker.MainViewModel
import com.josephlimbert.weighttracker.data.model.ErrorMessage
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
    fun signInWithEmail(email: String, password: String, showErrorSnackbar: (ErrorMessage) -> Unit) {
        launchCatching(showErrorSnackbar) {
            authRepository.signInWithEmail(email, password)
            _shouldRestartApp.value = true
        }
    }

    fun signUpWithEmail(email: String, password: String, showErrorSnackbar: (ErrorMessage) -> Unit) {
        launchCatching(showErrorSnackbar) {
            val user = authRepository.linkAccount(email, password)
            firestoreRepository.linkUserProfile(user!!)
            _shouldRestartApp.value = true
        }
    }
}