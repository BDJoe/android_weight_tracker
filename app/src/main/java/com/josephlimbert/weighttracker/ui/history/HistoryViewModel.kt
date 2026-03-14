package com.josephlimbert.weighttracker.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.josephlimbert.weighttracker.MainViewModel
import com.josephlimbert.weighttracker.data.model.Weight
import com.josephlimbert.weighttracker.data.repository.AuthRepository
import com.josephlimbert.weighttracker.data.repository.FirestoreRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import javax.inject.Inject

@HiltViewModel
class HistoryViewModel @Inject constructor(private val firestoreRepository: FirestoreRepository, authRepository: AuthRepository) : MainViewModel() {
    val weights = firestoreRepository.weightList
    val goalWeight = firestoreRepository.goalWeight
    val userId = authRepository.currentUserIdFlow

    fun deleteWeight(weightId: String) {
        launchCatching {
            firestoreRepository.deleteWeight(weightId)
        }
    }
}