package com.josephlimbert.weighttracker.ui

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.josephlimbert.weighttracker.MainViewModel
import com.josephlimbert.weighttracker.R
import com.josephlimbert.weighttracker.data.model.ErrorMessage
import com.josephlimbert.weighttracker.data.model.Weight
import com.josephlimbert.weighttracker.data.repository.AuthRepository
import com.josephlimbert.weighttracker.data.repository.WeightItemRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import javax.inject.Inject


@HiltViewModel
class WeightViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val weightItemRepository: WeightItemRepository
) : MainViewModel() {
    private val _isLoadingUser = MutableStateFlow(true)
    private val _weightList = MutableStateFlow<List<Weight>>(emptyList())
    private val _weight = MutableStateFlow<Weight?>(null)
    private val _currentWeight = MutableStateFlow<Weight?>(null)
    private val _startingWeight = MutableStateFlow<Weight?>(null)
    private val _goalWeight = MutableStateFlow<Double?>(null)

    val isLoadingUser: StateFlow<Boolean>
        get() = _isLoadingUser.asStateFlow()

    val weightList: StateFlow<List<Weight>>
        get() = _weightList.asStateFlow()

    val weight: StateFlow<Weight?>
        get() = _weight.asStateFlow()

    val currentWeight: StateFlow<Weight?>
        get() = _currentWeight.asStateFlow()

    val startingWeight: StateFlow<Weight?>
        get() = _startingWeight.asStateFlow()

    val goalWeight: StateFlow<Double?>
        get() = _goalWeight.asStateFlow()

    val totalLossPercent: StateFlow<Double> =
        combine(_startingWeight, _currentWeight, _goalWeight) { starting, current, goal ->
            val result = if (starting != null && current != null && goal != null) {
                (((starting.weight - current.weight) / (starting.weight - goal)) * 100.0).coerceAtMost(
                    100.0
                )
            } else { 0.0 }
            return@combine result
        } as StateFlow<Double>

    val totalLossWeight: StateFlow<Double> =
    combine(_startingWeight, _currentWeight) { starting, current ->
        val result = if (starting != null && current != null) {
            starting.weight - current.weight
        } else { 0.0 }
        return@combine result
    } as StateFlow<Double>

    val targetLoss: StateFlow<Double> =
        combine(_startingWeight, _goalWeight) { starting, goal ->
        val result = if (starting != null && goal != null) {
            starting.weight - goal
        } else { 0.0 }
        return@combine result
    } as StateFlow<Double>

    val targetLeft: StateFlow<Double> =
        combine(_startingWeight, _currentWeight, _goalWeight) { starting, current, goal ->
        val result = if (starting != null && current != null && goal != null) {
            (starting.weight - goal) - (starting.weight - current.weight)
        } else { 0.0 }
        return@combine result
    } as StateFlow<Double>

    init {
        loadWeightList()
    }

    private fun loadWeightList() {
        viewModelScope.launch {
            weightItemRepository.getWeightList(authRepository.currentUserIdFlow)
                .collect { weights ->
                    _weightList.value = weights
                    if (!weights.isEmpty()) {
                        _startingWeight.value = weights[weights.size - 1]
                        _currentWeight.value = weights[0]
                    }
                }
            if (authRepository.currentUser != null) {
                //TODO: add user info to user repo to get goal weight
            }
        }
    }

    fun loadCurrentUser() {
        launchCatching {
            if (authRepository.currentUser == null) {
                authRepository.createGuestAccount()
            }

            _isLoadingUser.value = false
        }
    }

    fun getWeight(weightId: String) {
        launchCatching {
            _weight.value = weightItemRepository.getWeight(weightId)
        }
    }

    fun addWeight(weight: Weight, showErrorSnackbar: (ErrorMessage) -> Unit) {
        val userId = authRepository.currentUser?.uid

        if (userId.isNullOrBlank()) {
            showErrorSnackbar(ErrorMessage.IdError(R.string.could_not_find_account))
            return
        }

        launchCatching {
            val id = hashId(weight.recordedDate.toString() + userId);
            if (weight.id.isBlank()) {
                weightItemRepository.addWeight(weight.copy(userId = userId, id = id))
            } else {
                weightItemRepository.deleteWeight(weight.id)
                weightItemRepository.addWeight(weight.copy(userId = userId, id = id))
            }
        }
    }

    fun deleteWeight(weightId: String) {
        launchCatching {
            weightItemRepository.deleteWeight(weightId)
        }
    }

    private fun hashId(input: String): String {
        try {
            val md = MessageDigest.getInstance("MD5")
            md.update(input.toByteArray())
            val digest = md.digest()
            val hexString = StringBuilder()
            for (b in digest) {
                hexString.append(Integer.toHexString(0xFF and b.toInt()))
            }
            return hexString.toString()
        } catch (e: NoSuchAlgorithmException) {
            throw RuntimeException(e)
        }
    }
}