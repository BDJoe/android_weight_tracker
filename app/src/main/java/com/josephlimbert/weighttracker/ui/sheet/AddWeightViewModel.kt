package com.josephlimbert.weighttracker.ui.sheet

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.josephlimbert.weighttracker.data.model.Weight
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
class AddWeightViewModel @Inject constructor(private val firestoreRepository: FirestoreRepository): ViewModel() {

    val user = firestoreRepository.userProfile
    private val _weight = MutableStateFlow<Weight?>(null)
    val weight: StateFlow<Weight?>
        get() = _weight.asStateFlow()

    suspend fun getWeight(weightId: String?): Weight {
        if (weightId != null) {
           val weight = firestoreRepository.getWeight(weightId)
            return weight ?: Weight()
        } else {
            return Weight()
        }
    }

    fun addWeight(userId: String, weight: Weight) {
        viewModelScope.launch {
            val id = hashId(weight.recordedDate.toString() + userId)
            if (weight.id.isBlank()) {
                firestoreRepository.addWeight(weight.copy(userId = userId, id = id))
            } else {
                firestoreRepository.deleteWeight(weight.id)
                firestoreRepository.addWeight(weight.copy(userId = userId, id = id))
            }
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