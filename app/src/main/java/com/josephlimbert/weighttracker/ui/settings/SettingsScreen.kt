package com.josephlimbert.weighttracker.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation3.runtime.NavKey
import com.josephlimbert.weighttracker.R
import com.josephlimbert.weighttracker.ui.sheet.SetGoalSheet
import kotlinx.serialization.Serializable

@Serializable
data object Settings : NavKey

@Composable
fun SettingsScreen(modifier: Modifier, onNavigateToSetGoal: () -> Unit, onSignOUt: () -> Unit, viewModel: SettingsViewModel = hiltViewModel()) {
    SettingsScreenContent(
        modifier = modifier,
        onNavigateToSetGoal = onNavigateToSetGoal,
        onSignOUt = {
            viewModel.signOut()
            onSignOUt()
        })
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreenContent(modifier: Modifier, onNavigateToSetGoal: () -> Unit, onSignOUt: () -> Unit) {
    val scrollState = rememberScrollState()

    Scaffold() { innerPadding ->
        Column(modifier = modifier.fillMaxSize()
            .padding(
            top = innerPadding.calculateTopPadding(),
            start = 4.dp,
            end = 4.dp,
            bottom = 4.dp
        )
            .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
            ) {
            Button(onClick = onNavigateToSetGoal, modifier = Modifier.padding(bottom = 30.dp)) {
                Text(text = stringResource(R.string.change_goal_weight))
            }
            Button(onClick = onSignOUt, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)) {
                Text(text = stringResource(R.string.sign_out))
            }
        }
    }
}

@Composable
@Preview(showSystemUi = false)
fun SettingsScreenPreview() {
    MaterialTheme() {
        SettingsScreenContent(modifier = Modifier, onNavigateToSetGoal = {}, onSignOUt = {})
    }
}