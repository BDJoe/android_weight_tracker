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
import com.josephlimbert.weighttracker.data.model.User
import com.josephlimbert.weighttracker.data.model.Weight
import kotlinx.serialization.Serializable

@Serializable
data object Auth : NavKey

enum class AuthMethod(val label: String) {
    SIGN_IN(label = "Sign In"),
    SIGN_UP(label = "Register"),
}

enum class WeightUnit(val label: String, val value: String) {
    POUND(label = "Pound (lb)", value = " lbs"),
    KILO(label = "Kilogram (kg)", value = " kgs")
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
    val userId by viewModel.userId.collectAsStateWithLifecycle(null)

    if (userId != null) {
        openHomeScreen()
    } else {
        AuthScreenContent(
            signIn = viewModel::signInWithEmail,
            signUp = viewModel::signUpWithEmail,
            signUpGuest = viewModel::createGuestAccount,
            showErrorSnackbar = showErrorSnackbar)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthScreenContent(
    signIn: (String, String, (ErrorMessage) -> Unit) -> Unit,
    signUp: (email: String, password: String, name: String, goalWeight: Double, weightUnit: String, (ErrorMessage) -> Unit) -> Unit,
    signUpGuest: (name: String, goalWeight: Double, weightUnit: String, (ErrorMessage) -> Unit) -> Unit,
    showErrorSnackbar: (ErrorMessage) -> Unit
) {
    var currentPage by remember { mutableStateOf(Layout.AUTHENTICATE) }
    val emailState: TextFieldState = rememberTextFieldState("")
    val passwordState: TextFieldState = rememberTextFieldState("")
    val confirmPasswordState: TextFieldState = rememberTextFieldState("")
    val nameState: TextFieldState = rememberTextFieldState("")
    val goalWeightState: TextFieldState = rememberTextFieldState("")
    val weightUnitState = remember { mutableStateOf(WeightUnit.POUND) }
    var isGuest by remember { mutableStateOf(false) }

    Scaffold(
        topBar = { CenterAlignedTopAppBar(
            title = { when (currentPage) {
                Layout.AUTHENTICATE -> Text("Sign In/Register")
                Layout.PROFILE -> Text("Create Profile")
            } },
            navigationIcon = {
                if (currentPage == Layout.PROFILE) {
                   IconButton(onClick = { currentPage = Layout.AUTHENTICATE}) {
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
                    emailState = emailState,
                    passwordState = passwordState,
                    confirmPasswordState = confirmPasswordState,
                    signIn = { signIn(emailState.text.toString(), passwordState.text.toString(), showErrorSnackbar) },
                    signUp = {
                        currentPage = Layout.PROFILE
                    },
                    signUpGuest = {
                        currentPage = Layout.PROFILE
                        isGuest = true
                    }
                )

                Layout.PROFILE -> CreateProfile(
                    modifier = modifier,
                    nameState = nameState,
                    goalWeightState = goalWeightState,
                    weightUnitState = weightUnitState,
                    signUp = {
                        if (isGuest) {
                            signUpGuest(
                                nameState.text.toString(),
                                goalWeightState.text.toString().toDouble(),
                                weightUnitState.value.value,
                                showErrorSnackbar
                            )
                        } else {
                            signUp(
                                emailState.text.toString(),
                                passwordState.text.toString(),
                                nameState.text.toString(),
                                goalWeightState.text.toString().toDouble(),
                                weightUnitState.value.value,
                                showErrorSnackbar
                            )
                        }
                    })
            }
        }
    }
}

@Composable
fun Authenticate(
    modifier: Modifier,
    emailState: TextFieldState,
    passwordState: TextFieldState,
    confirmPasswordState: TextFieldState,
    signIn: () -> Unit,
    signUp: () -> Unit,
    signUpGuest: () -> Unit
) {
    var selectedMethod by remember { mutableStateOf<AuthMethod>(AuthMethod.SIGN_IN) }
    val options = AuthMethod.entries
    val isMatchingPassword = if (selectedMethod == AuthMethod.SIGN_UP && confirmPasswordState.text.isNotEmpty()) {
        passwordState.text == confirmPasswordState.text
    } else true
    var isSubmitting by remember { mutableStateOf(false) }

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        SingleChoiceSegmentedButtonRow {
            options.forEachIndexed { index, method ->
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
                .padding(10.dp),
            state = passwordState,
            label = { Text("Password") },
        )
        AnimatedVisibility(selectedMethod == AuthMethod.SIGN_UP) {
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
        Button(
            onClick = {
            isSubmitting = true
            if (isMatchingPassword && selectedMethod == AuthMethod.SIGN_IN
            ) {
                signIn()
            } else if (isMatchingPassword && selectedMethod == AuthMethod.SIGN_UP) {
                signUp()
            }
            isSubmitting = false
        }, modifier = Modifier.padding(top = 50.dp), enabled = !isSubmitting) {
            when (selectedMethod) {
                AuthMethod.SIGN_IN -> {
                    Text(selectedMethod.label, style = MaterialTheme.typography.titleLarge)
                }

                AuthMethod.SIGN_UP -> {
                    Text(selectedMethod.label, style = MaterialTheme.typography.titleLarge)
                }
            }
        }

        Button(
            onClick = signUpGuest, modifier = Modifier.padding(top = 20.dp), enabled = !isSubmitting,
            colors = ButtonDefaults.filledTonalButtonColors()) {
            Text("Continue as Guest", style = MaterialTheme.typography.titleLarge)
        }
    }
}

@Composable
fun CreateProfile(
    modifier: Modifier,
    nameState: TextFieldState,
    goalWeightState: TextFieldState,
    weightUnitState: MutableState<WeightUnit>,
    signUp: () -> Unit) {
    val options = listOf("Pound (lb)", "Kilogram (kg)")
    var selectedOption by remember { mutableIntStateOf(0) }

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        OutlinedTextField(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 40.dp, start = 10.dp, end = 10.dp),
            state = nameState,
            label = { Text("Name") }
        )
        OutlinedTextField(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 40.dp, start = 10.dp, end = 10.dp),
            state = goalWeightState,
            label = { Text("Goal Weight") },
            supportingText = { Text("This can be changed later")}
        )

        Row(modifier = Modifier
            .fillMaxWidth()
            .padding(top = 40.dp, start = 20.dp, end = 20.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Weight Unit", style = MaterialTheme.typography.titleMedium)
            SingleChoiceSegmentedButtonRow {
                WeightUnit.entries.forEachIndexed { index, unit ->
                    SegmentedButton(
                        shape = SegmentedButtonDefaults.itemShape(
                            index = index,
                            count = options.size
                        ),
                        onClick =
                            {
                                weightUnitState.value = unit
                            },
                        selected = index == selectedOption,
                        label = { Text(unit.label) }
                    )
                }
            }
        }

        Button(onClick = signUp, modifier = Modifier.padding(top = 40.dp)) {
            Text("Submit", style = MaterialTheme.typography.titleLarge)
        }
    }
}

@Composable
@Preview(showSystemUi = true)
fun AuthScreenContentPreview() {
        AuthScreenContent(signIn = {_,_,_ ->}, signUp = {_,_,_,_,_,_->}, signUpGuest = {_,_,_,_ -> }, showErrorSnackbar = {})
}