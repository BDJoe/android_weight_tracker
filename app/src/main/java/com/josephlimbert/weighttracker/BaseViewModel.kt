package com.josephlimbert.weighttracker

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.josephlimbert.weighttracker.data.model.ErrorMessage
import com.josephlimbert.weighttracker.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject

//open class BaseViewModel : ViewModel() {
//    fun launchCatching(
//        showErrorSnackbar: (ErrorMessage) -> Unit = {},
//        block: suspend CoroutineScope.() -> Unit
//    ) =
//        viewModelScope.launch(
//            CoroutineExceptionHandler { _, throwable ->
//                val error = if (throwable.message.isNullOrBlank()) {
//                    ErrorMessage.IdError(R.string.generic_error)
//                } else {
//                    ErrorMessage.StringError(throwable.message!!)
//                }
//                showErrorSnackbar(error)
//            },
//            block = block
//        )
//}