package com.josephlimbert.weighttracker.ui.sheet

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.NavKey
import com.josephlimbert.weighttracker.R
import com.josephlimbert.weighttracker.ui.shared.LoadingIndicator
import kotlinx.serialization.Serializable

@Serializable
data object SetGoal: NavKey
@Composable
fun SetGoalSheet(onDismiss: () -> Unit, goalWeight: Double? = null, viewModel: SetGoalViewModel = hiltViewModel()) {
    val user = viewModel.user.collectAsStateWithLifecycle(null)
    val goalWeight = viewModel.goalWeight.collectAsStateWithLifecycle(null)

    if (goalWeight.value == null) {
        LoadingIndicator()
    } else {
        SetGoalContent(onSubmit = { weight ->
            viewModel.setGoalWeight(user.value!!.uid, weight.toDouble())
            onDismiss()
        }, goalWeight = goalWeight.value)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SetGoalContent(onSubmit: (weight: String) -> Unit, goalWeight: Double?) {
    val goalText = if (goalWeight == null || goalWeight <= 0) "" else goalWeight.toString()
    val weightState = rememberTextFieldState(goalText)

    Column(modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = stringResource(R.string.set_goal_weight), style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.W500)
        OutlinedTextField(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 15.dp, start = 15.dp, end = 15.dp, bottom = 10.dp),
            state = weightState,
            label = { Text("Weight") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
        )
        Button(onClick = { onSubmit(weightState.text.toString()) }, modifier = Modifier.padding(top = 50.dp)) {
            Text("Submit")
        }
    }
}

@Composable
@Preview(showSystemUi = false)
fun SetGoalSheetPreview() {
    SetGoalSheet(onDismiss = {})
}