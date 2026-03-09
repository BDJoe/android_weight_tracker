package com.josephlimbert.weighttracker.ui.signin

import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseUser
import com.josephlimbert.weighttracker.data.repository.AuthRepository
import com.josephlimbert.weighttracker.data.repository.AuthResult
import com.josephlimbert.weighttracker.data.repository.FirestoreRepository
import com.josephlimbert.weighttracker.data.repository.FirestoreResult
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SignInViewModel @Inject constructor(private val firestoreRepository: FirestoreRepository, private val authRepository: AuthRepository): ViewModel() {
    suspend fun signInWithEmail(email: String, password: String): AuthResult<FirebaseUser> {
        return authRepository.signInWithEmail(email, password)
    }

    suspend fun signUpWithEmail(email: String, password: String): AuthResult<FirebaseUser> {
        return authRepository.linkAccount(email, password)
    }

    suspend fun linkUserProfile(user: FirebaseUser): FirestoreResult<Unit> {
        return firestoreRepository.linkUserProfile(user)
    }
}