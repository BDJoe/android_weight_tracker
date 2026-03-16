package com.josephlimbert.weighttracker.ui.auth

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.clearText
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.Button
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
data object Auth : NavKey

enum class AuthMethod(val value: Int) {
    SIGN_IN(value = 0),
    SIGN_UP(value = 1),
}

enum class Layout() {
    AUTHENTICATE,
    PROFILE
}

@Composable
fun AuthScreen(
    openHomeScreen: () -> Unit,
    showErrorSnackbar: (ErrorMessage) -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val shouldRestartApp by viewModel.shouldRestartApp.collectAsStateWithLifecycle()

    if (shouldRestartApp) {
        openHomeScreen()
    } else {
        AuthScreenContent(
            onCloseDialog = openHomeScreen,
            signIn = {_, _, _ ->
                openHomeScreen()
            },
            signUp = {_, _, _ ->

            },
            showErrorSnackbar = showErrorSnackbar
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthScreenContent(
    onCloseDialog: () -> Unit,
    signIn: (String, String, (ErrorMessage) -> Unit) -> Unit,
    signUp: (String, String, (ErrorMessage) -> Unit) -> Unit,
    showErrorSnackbar: (ErrorMessage) -> Unit
) {
    var currentPage by remember { mutableStateOf(Layout.AUTHENTICATE) }

    Scaffold(
        topBar = { CenterAlignedTopAppBar(
            title = { when (currentPage) {
                Layout.AUTHENTICATE -> Text("Log In/Register")
                Layout.PROFILE -> Text("Create Profile")
            } },
            navigationIcon = {
                when (currentPage) {
                    Layout.AUTHENTICATE -> IconButton(onClick = onCloseDialog) {
                        Icon (
                            ImageVector.vectorResource(R.drawable.close_icon),
                            contentDescription = "close log in screen"
                        )
                    }

                    Layout.PROFILE -> IconButton(onClick = { currentPage = Layout.AUTHENTICATE}) {
                        Icon(
                            ImageVector.vectorResource(R.drawable.back_arrow_icon),
                            contentDescription = "return to log in"
                        )
                    }
                }

            }
        ) }
    ) { innerPadding ->
        val modifier = Modifier
            .fillMaxSize()
            .padding(
                top = innerPadding.calculateTopPadding(),
                bottom = 4.dp,
                start = 4.dp,
                end = 4.dp
            )

        Crossfade(targetState = currentPage, label = "auth layout fade") { screen ->
            when (screen) {
                Layout.AUTHENTICATE ->  Authenticate(
                    modifier = modifier,
                    signIn = signIn,
                    signUp = { email, password, showErrorSnackbar ->
                        currentPage = Layout.PROFILE
                        signUp(email, password, showErrorSnackbar)
                    },
                    showErrorSnackbar = showErrorSnackbar
                )

                Layout.PROFILE -> CreateProfile(modifier = modifier)
            }
        }
    }
}

@Composable
fun Authenticate(
    modifier: Modifier,
    signIn: (String, String, (ErrorMessage) -> Unit) -> Unit,
    signUp: (String, String, (ErrorMessage) -> Unit) -> Unit,
    showErrorSnackbar: (ErrorMessage) -> Unit
) {
    var selectedIndex by remember { mutableIntStateOf(AuthMethod.SIGN_IN.value) }
    val options = listOf("Sign In", "Register")
    val emailState: TextFieldState = rememberTextFieldState("")
    val passwordState: TextFieldState = rememberTextFieldState("")
    val confirmPasswordState: TextFieldState = rememberTextFieldState("")
    val isMatchingPassword = if (selectedIndex == 1 && confirmPasswordState.text.isNotEmpty()) {
        passwordState.text == confirmPasswordState
    } else true
    var isSubmitting by remember { mutableStateOf(false) }

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        SingleChoiceSegmentedButtonRow {
            options.forEachIndexed { index, label ->
                SegmentedButton(
                    shape = SegmentedButtonDefaults.itemShape(
                        index = index,
                        count = options.size
                    ),
                    onClick =
                        {
                            emailState.clearText()
                            passwordState.clearText()
                            confirmPasswordState.clearText()
                            selectedIndex = index
                        },
                    selected = index == selectedIndex,
                    label = { Text(label) }
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
                .padding(10.dp),
            state = passwordState,
            label = { Text("Password") },
        )
        AnimatedVisibility(selectedIndex == AuthMethod.SIGN_UP.value) {
            OutlinedSecureTextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp),
                state = confirmPasswordState,
                label = { Text("Confirm Password") },
                isError = !isMatchingPassword,
                supportingText = {
                    if (!isMatchingPassword) {
                        Text(
                            text = "Password doesn't match!",
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                },
            )
        }
        Button(onClick = {
            isSubmitting = true
            if (isMatchingPassword && selectedIndex == AuthMethod.SIGN_IN.value
            ) {
                signIn(emailState.text.toString(), passwordState.text.toString(), showErrorSnackbar)
            } else if (isMatchingPassword && selectedIndex == AuthMethod.SIGN_UP.value) {
                signUp(emailState.text.toString(), passwordState.text.toString(), showErrorSnackbar)
            }
            passwordState.clearText()
            confirmPasswordState.clearText()
            isSubmitting = false
        }, modifier = Modifier.padding(top = 50.dp), enabled = !isSubmitting) {
            Text("Submit")
        }
    }
}

@Composable
fun CreateProfile(modifier: Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Create A New Profile")
        OutlinedTextField(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 40.dp, start = 10.dp, end = 10.dp),
            state = TextFieldState(),
            label = { Text("Name") }
        )
        OutlinedTextField(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 40.dp, start = 10.dp, end = 10.dp),
            state = TextFieldState(),
            label = { Text("Goal Weight") }
        )
        OutlinedTextField(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 40.dp, start = 10.dp, end = 10.dp),
            state = TextFieldState(),
            label = { Text("Preferred Weight Units") }
        )
    }
}

@Composable
@Preview(showSystemUi = true)
fun AuthScreenContentPreview() {
    MaterialTheme() {
        AuthScreenContent(signIn = {_,_,_ ->}, signUp = {_,_,_->}, onCloseDialog = {}, showErrorSnackbar = {})
    }
}