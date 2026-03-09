package com.josephlimbert.weighttracker.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.josephlimbert.weighttracker.data.model.Weight
import com.josephlimbert.weighttracker.data.repository.AuthRepository
import com.josephlimbert.weighttracker.data.repository.FirestoreRepository
import com.josephlimbert.weighttracker.data.repository.FirestoreResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class UiState(
    val isLoading: Boolean = true,
    val weightList: List<Weight> = emptyList(),
    val goalWeight: Double? = null
)

@HiltViewModel
class HistoryViewModel @Inject constructor(private val firestoreRepository: FirestoreRepository, private val authRepository: AuthRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState>
        get() = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            viewModelScope.launch {
                firestoreRepository.weightList.collect { weights ->
                    if (weights == null) return@collect
                    _uiState.value = _uiState.value.copy(weightList = weights, isLoading = false)
                }
            }
            viewModelScope.launch {
                firestoreRepository.userProfile.collect { user ->
                    if (user != null) {
                        _uiState.value = _uiState.value.copy(goalWeight = user.goalWeight)
                    }
                }
            }
        }
    }

    suspend fun deleteWeight(weightId: String): FirestoreResult<Unit> {
        return firestoreRepository.deleteWeight(weightId)
    }
}