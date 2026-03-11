package com.josephlimbert.weighttracker.ui.signin

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedSecureTextField
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseUser
import com.josephlimbert.weighttracker.R
import com.josephlimbert.weighttracker.data.repository.AuthResult
import kotlinx.serialization.Serializable

@Serializable
object AuthRoute

@Composable
fun AuthScreen(modifier: Modifier) {
    AuthScreenContent(modifier = modifier)
}

@Composable
fun AuthScreenContent(modifier: Modifier) {
    val emailState: TextFieldState = TextFieldState()
    val passwordState: TextFieldState = TextFieldState()

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
            Text(text = stringResource(R.string.login_description),
                modifier = Modifier.padding(top = 30.dp, start = 10.dp, end = 10.dp),
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
                label = { Text("Password") }
            )
            Button(onClick = { }, modifier = Modifier.padding(top = 50.dp)) {
                Text("Submit")
            }
        }
    }
}

@Composable
@Preview(showSystemUi = true)
fun AuthScreenContentPreview() {
    MaterialTheme() {
        AuthScreenContent(modifier = Modifier)
    }
}