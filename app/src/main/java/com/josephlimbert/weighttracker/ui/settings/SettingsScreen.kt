package com.josephlimbert.weighttracker.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.josephlimbert.weighttracker.R

@Composable
fun SettingsScreen() {
    SettingsScreenContent()
}

@Composable
fun SettingsScreenContent() {
    Scaffold() { innerPadding ->
        Column(modifier = Modifier.fillMaxSize()
            .padding(
            top = innerPadding.calculateTopPadding(),
            start = 4.dp,
            end = 4.dp,
            bottom = 4.dp
        ),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
            ) {
            Button(onClick = {}, modifier = Modifier.padding(bottom = 30.dp)) {
                Text(text = stringResource(R.string.change_goal_weight))
            }
            Button(onClick = {}, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)) {
                Text(text = stringResource(R.string.sign_out))
            }
        }
    }
}

@Composable
@Preview(showSystemUi = true)
fun SettingsScreenPreview() {
    MaterialTheme() {
        SettingsScreenContent()
    }
}