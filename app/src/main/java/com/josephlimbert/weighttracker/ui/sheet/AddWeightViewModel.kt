package com.josephlimbert.weighttracker.ui.sheet

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseUser
import com.josephlimbert.weighttracker.data.model.User
import com.josephlimbert.weighttracker.data.model.Weight
import com.josephlimbert.weighttracker.data.repository.AuthRepository
import com.josephlimbert.weighttracker.data.repository.FirestoreRepository
import com.josephlimbert.weighttracker.data.repository.FirestoreResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import javax.inject.Inject

@HiltViewModel
class AddWeightViewModel @Inject constructor(private val firestoreRepository: FirestoreRepository, private val authRepository: AuthRepository): ViewModel() {
    val user = authRepository.authStateFlow

    suspend fun addWeight(userId: String, weight: Weight): FirestoreResult<Unit> {
        val id = hashId(weight.recordedDate.toString() + userId);
        if (weight.id.isBlank()) {
            return firestoreRepository.addWeight(weight.copy(userId = userId, id = id))
        } else {
            firestoreRepository.deleteWeight(weight.id)
            return firestoreRepository.addWeight(weight.copy(userId = userId, id = id))
        }
    }

    suspend fun getWeight(weightId: String): FirestoreResult<Weight?> {
        return firestoreRepository.getWeight(weightId)
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