package com.josephlimbert.weighttracker.data.repository

import com.google.firebase.auth.FirebaseUser
import com.josephlimbert.weighttracker.data.datasource.AuthRemoteDataSource
import com.josephlimbert.weighttracker.data.model.User
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class AuthRepository @Inject constructor(
    private val authRemoteDataSource: AuthRemoteDataSource
) {
    val currentUser: FirebaseUser? = authRemoteDataSource.currentUser
    val currentUserIdFlow: Flow<String?> = authRemoteDataSource.currentUserIdFlow

    suspend fun createGuestAccount() {
        authRemoteDataSource.createGuestAccount()
    }

    suspend fun signInWithEmail(email: String, password: String) {
        authRemoteDataSource.signInWithEmail(email, password)
    }

    suspend fun signUpWithEmail(email: String, password: String) {
        authRemoteDataSource.linkAccount(email, password)
    }

    suspend fun getUserProfile(userId: String): User? {
        return authRemoteDataSource.getUserProfile(userId)
    }

    fun signOut() {
        authRemoteDataSource.signOut()
    }

    suspend fun deleteAccount() {
        authRemoteDataSource.deleteAccount()
    }
}