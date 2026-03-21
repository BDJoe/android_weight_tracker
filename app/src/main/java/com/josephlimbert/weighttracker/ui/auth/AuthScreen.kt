package com.josephlimbert.weighttracker.ui.auth

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.clearText
import androidx.compose.foundation.text.input.rememberTextFieldState
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import com.josephlimbert.weighttracker.R
import com.josephlimbert.weighttracker.data.model.ErrorMessage
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
    val user by viewModel.user.collectAsStateWithLifecycle("")

    AuthScreenContent(
        isGuest = isGuest,
        signIn = { email, password ->
            viewModel.signInWithEmail(email, password, showErrorSnackbar, openHomeScreen)
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
    signIn: (String, String) -> Unit,
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
    var isSubmitting by remember { mutableStateOf(false) }

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
            if(!isGuest) {
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
            }
            Text(
                text =
                    if (isGuest)
                        "Create a new account to sync your data across devices"
                    else
                        stringResource(R.string.login_description),
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
            AnimatedVisibility(selectedMethod == AuthMethod.SIGN_UP || isGuest) {
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
            Button(
                onClick = {
                    isSubmitting = true
                    if (selectedMethod == AuthMethod.SIGN_IN
                    ) {
                        signIn(emailState.text.toString(), passwordState.text.toString())
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
                    isSubmitting = false
                }, modifier = Modifier.padding(top = 50.dp), enabled = !isSubmitting
            ) {
                Text(selectedMethod.label, style = MaterialTheme.typography.titleLarge)
            }

            if (!isGuest) {
                Button(
                    onClick = signUpGuest,
                    modifier = Modifier.padding(top = 20.dp),
                    enabled = !isSubmitting,
                    colors = ButtonDefaults.filledTonalButtonColors()
                ) {
                    Text("Continue as Guest", style = MaterialTheme.typography.titleLarge)
                }
            }
        }
    }
}

@Composable
@Preview(showSystemUi = true)
fun AuthScreenContentPreview() {
        AuthScreenContent(
            isGuest = true,
            signIn = {_,_, ->},
            signUp = {_,_->},
            signUpGuest = { -> },
            linkAccount = {_,_ ->},
            closeAuth = {})
}