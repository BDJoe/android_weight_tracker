package com.josephlimbert.weighttracker.ui.auth

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.clearText
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedSecureTextField
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.NavKey
import com.google.firebase.auth.FirebaseUser
import com.josephlimbert.weighttracker.R
import kotlinx.serialization.Serializable

@Serializable
data class Auth(val isGuest: Boolean) : NavKey

enum class AuthMethod(val label: String) {
    SIGN_IN(label = "Sign In"),
    SIGN_UP(label = "Register"),
}

@Composable
fun AuthScreen(
    openHomeScreen: () -> Unit,
    showErrorSnackbar: (String) -> Unit,
    closeAuth: () -> Unit,
    isGuest: Boolean,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val user: FirebaseUser? by viewModel.user.collectAsStateWithLifecycle(null)
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle(false)

    AuthScreenContent(
        isGuest = isGuest,
        isLoading = isLoading,
        signIn = { email, password ->
            viewModel.signInWithEmail(email, password, showErrorSnackbar, openHomeScreen)
                 },
        signInDeleteGuest = { email, password ->
            viewModel.signInEmailAndDeleteGuest(email, password, user!!.uid, showErrorSnackbar, openHomeScreen)

        },
        signUp = { email, password ->
            viewModel.signUpWithEmail(email, password, showErrorSnackbar, openHomeScreen)
                 },
        signUpGuest = {
            viewModel.createGuestAccount(showErrorSnackbar, openHomeScreen)
                      },
        linkAccount = { email, password ->
            viewModel.linkAccount(email, password, showErrorSnackbar, openHomeScreen)
                      },
        closeAuth = closeAuth
        )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthScreenContent(
    isGuest: Boolean,
    isLoading: Boolean,
    signIn: (String, String) -> Unit,
    signInDeleteGuest: (String, String) -> Unit,
    signUp: (email: String, password: String) -> Unit,
    signUpGuest: () -> Unit,
    closeAuth: () -> Unit,
    linkAccount: (String, String) -> Unit
) {
    val emailState: TextFieldState = rememberTextFieldState("")
    val passwordState: TextFieldState = rememberTextFieldState("")
    val confirmPasswordState: TextFieldState = rememberTextFieldState("")
    var selectedMethod by remember {
        mutableStateOf(if (isGuest) AuthMethod.SIGN_UP else AuthMethod.SIGN_IN)
    }
    val authMethods = AuthMethod.entries
    val isMatchingPassword =
        if (selectedMethod == AuthMethod.SIGN_UP && confirmPasswordState.text.isNotEmpty()) {
            passwordState.text == confirmPasswordState.text
        } else true
    var showError by remember { mutableStateOf(false) }
    var showModal by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Welcome to Weight Tracker") },
                navigationIcon = {
                    // Show the close button if we are linking an account
                    if (isGuest) {
                        IconButton(onClick = closeAuth) {
                            Icon(
                                ImageVector.vectorResource(R.drawable.close_icon),
                                contentDescription = "close"
                            )
                        }
                    }
                                 }
            )
        }
    ) { innerPadding ->
        val modifier = Modifier
            .fillMaxSize()
            .padding(
                top = innerPadding.calculateTopPadding(),
                bottom = 4.dp,
                start = 4.dp,
                end = 4.dp
            )

        Column(
            modifier = modifier,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
                SingleChoiceSegmentedButtonRow {
                    authMethods.forEachIndexed { index, method ->
                        SegmentedButton(
                            shape = SegmentedButtonDefaults.itemShape(
                                index = index,
                                count = authMethods.size
                            ),
                            onClick =
                                {
                                    emailState.clearText()
                                    passwordState.clearText()
                                    confirmPasswordState.clearText()
                                    selectedMethod = method
                                },
                            selected = method == selectedMethod,
                            label = { Text(method.label) }
                        )
                    }
                }
            Text(
                text = stringResource(R.string.login_description),
                modifier = Modifier.padding(top = 10.dp, start = 10.dp, end = 10.dp),
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
            )
            OutlinedTextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 40.dp, start = 10.dp, end = 10.dp),
                state = emailState,
                label = { Text("Email") }
            )
            OutlinedSecureTextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp, start = 10.dp, end = 10.dp),
                state = passwordState,
                label = { Text("Password") },
            )
            AnimatedVisibility(selectedMethod == AuthMethod.SIGN_UP) {
                OutlinedSecureTextField(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp, start = 10.dp, end = 10.dp),
                    state = confirmPasswordState,
                    label = { Text("Confirm Password") },
                    isError = showError && !isMatchingPassword,
                    supportingText = {
                        if (showError && !isMatchingPassword) {
                            Text(
                                text = "Password doesn't match!",
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    },
                )
            }

            if (showModal) {
                ConfirmLoginDialog(
                    onDismissRequest = {
                        showModal = false
                    },
                    onConfirmation = {
                        signInDeleteGuest(emailState.text.toString(), passwordState.text.toString())
                    }
                )
            }

            Button(
                onClick = {
                    if (selectedMethod == AuthMethod.SIGN_IN
                    ) {
                        if (isGuest) {
                            showModal = true
                        } else {
                            signIn(emailState.text.toString(), passwordState.text.toString())
                        }
                    } else if (selectedMethod == AuthMethod.SIGN_UP) {
                        if (isMatchingPassword) {
                            if (isGuest) {
                                linkAccount(emailState.text.toString(), passwordState.text.toString())
                            } else {
                                signUp(emailState.text.toString(), passwordState.text.toString())
                            }
                        } else {
                            showError = true
                        }
                    }
                }, modifier = Modifier.padding(top = 50.dp), enabled = !isLoading
            ) {
                Text(selectedMethod.label, style = MaterialTheme.typography.titleLarge)
            }

            if (!isGuest) {
                Button(
                    onClick = signUpGuest,
                    modifier = Modifier.padding(top = 20.dp),
                    enabled = !isLoading,
                    colors = ButtonDefaults.filledTonalButtonColors()
                ) {
                    Text("Continue as Guest", style = MaterialTheme.typography.titleLarge)
                }
            }
        }
    }
}

@Composable
fun ConfirmLoginDialog(
    onDismissRequest: () -> Unit,
    onConfirmation: () -> Unit,
) {
    AlertDialog(
        text = {
            Text(text = "Signing in to an existing account will delete all the data currently associated with this device!")
        },
        onDismissRequest = {
            onDismissRequest()
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirmation()
                }
            ) {
                Text(text = "Delete", color = MaterialTheme.colorScheme.error)
            }
        },
        dismissButton = {
            TextButton(
                onClick = {
                    onDismissRequest()
                }
            ) {
                Text("Cancel")
            }
        }
    )
}

@Composable
@Preview(showSystemUi = true)
fun AuthScreenContentPreview() {
        AuthScreenContent(
            isGuest = true,
            isLoading = false,
            signIn = { _, _ ->},
            signInDeleteGuest = { _, _ ->},
            signUp = {_,_->},
            signUpGuest = { },
            linkAccount = {_,_ ->},
            closeAuth = {})
}