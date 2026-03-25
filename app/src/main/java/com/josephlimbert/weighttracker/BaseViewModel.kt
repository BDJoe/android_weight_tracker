package com.josephlimbert.weighttracker

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