package com.josephlimbert.weighttracker.ui

import androidx.lifecycle.SavedStateHandle
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
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import javax.inject.Inject


@HiltViewModel
class WeightViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val authRepository: AuthRepository,
    private val weightItemRepository: WeightItemRepository
) : MainViewModel() {
    private val _isLoadingUser = MutableStateFlow(true)
    private val _weight = MutableStateFlow<Weight?>(null)
    val isLoadingUser: StateFlow<Boolean>
        get() = _isLoadingUser.asStateFlow()

    val weightList = weightItemRepository.getWeightList(authRepository.currentUserIdFlow)
    val weight: StateFlow<Weight?>
        get() = _weight.asStateFlow()

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