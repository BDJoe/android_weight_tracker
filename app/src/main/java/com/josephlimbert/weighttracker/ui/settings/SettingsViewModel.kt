package com.josephlimbert.weighttracker.ui.settings

import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseUser
import com.josephlimbert.weighttracker.MainViewModel
import com.josephlimbert.weighttracker.data.repository.AuthRepository
import com.josephlimbert.weighttracker.data.model.User
import com.josephlimbert.weighttracker.data.repository.FirestoreRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val authRepository: AuthRepository, private val firestoreRepository: FirestoreRepository
) : MainViewModel() {
    val user = authRepository.currentUser

    fun signOut() {
        authRepository.signOut()
        if (user!!.isAnonymous) {
            launchCatching {
                firestoreRepository.deleteUserProfile(user.uid)
            }
        }
    }
}