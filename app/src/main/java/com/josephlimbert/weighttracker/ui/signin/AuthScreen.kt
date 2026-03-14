package com.josephlimbert.weighttracker.ui.signin

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.clearText
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.Button
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
import androidx.compose.ui.res.stringResource
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

@Composable
fun AuthScreen(
    modifier: Modifier,
    openHomeScreen: () -> Unit,
    showErrorSnackbar: (ErrorMessage) -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val shouldRestartApp by viewModel.shouldRestartApp.collectAsStateWithLifecycle()

    if (shouldRestartApp) {
        openHomeScreen()
    } else {
        AuthScreenContent(modifier = modifier,
            viewModel::signInWithEmail,
            viewModel::signUpWithEmail,
            showErrorSnackbar = showErrorSnackbar
        )
    }
}

@Composable
fun AuthScreenContent(
    modifier: Modifier,
    signIn: (String, String, (ErrorMessage) -> Unit) -> Unit,
    signUp: (String, String, (ErrorMessage) -> Unit) -> Unit,
    showErrorSnackbar: (ErrorMessage) -> Unit
) {
    var selectedIndex by remember { mutableIntStateOf(0) }
    val options = listOf("Sign In", "Register")
    val emailState: TextFieldState = rememberTextFieldState("")
    val passwordState: TextFieldState = rememberTextFieldState("")
    val confirmPasswordState: TextFieldState = rememberTextFieldState("")
    val isMatchingPassword = if (selectedIndex == 1 && confirmPasswordState.text.isNotEmpty()) {
        passwordState.text == confirmPasswordState
    } else true
    var isSubmitting by remember { mutableStateOf(false) }

    Scaffold() { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(
                    top = innerPadding.calculateTopPadding(),
                    start = 4.dp,
                    end = 4.dp,
                    bottom = 4.dp
                ),
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
            Text(text = stringResource(R.string.login_description),
                modifier = Modifier.padding(top = 10.dp, start = 10.dp, end = 10.dp),
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
            )
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth().padding(top = 40.dp, start = 10.dp, end = 10.dp),
                state = emailState,
                label = { Text("Email") }
            )
            OutlinedSecureTextField(
                modifier = Modifier.fillMaxWidth().padding(10.dp),
                state = passwordState,
                label = { Text("Password") },
            )
            if (selectedIndex == 1) {
                OutlinedSecureTextField(
                    modifier = Modifier.fillMaxWidth().padding(10.dp),
                    state = confirmPasswordState,
                    label = { Text("Confirm Password") },
                    isError = !isMatchingPassword,
                    supportingText = {
                        if (!isMatchingPassword) {
                            Text(text = "Password doesn't match!", color = MaterialTheme.colorScheme.error)
                        }
                    },
                )
            }
            Button(onClick = {
                isSubmitting = true
                if (isMatchingPassword && selectedIndex == 0) {
                    signIn(emailState.text.toString(), passwordState.text.toString(), showErrorSnackbar)
                } else if (isMatchingPassword && selectedIndex == 1) {
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
}

@Composable
@Preview(showSystemUi = true)
fun AuthScreenContentPreview() {
    MaterialTheme() {
        AuthScreenContent(modifier = Modifier, signIn = {_,_,_ ->}, signUp = {_,_,_->}, showErrorSnackbar = {})
    }
}