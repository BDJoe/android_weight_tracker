package com.josephlimbert.weighttracker.ui.sheet

import android.util.Log
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.NavKey
import com.josephlimbert.weighttracker.R
import com.josephlimbert.weighttracker.data.model.Weight
import com.josephlimbert.weighttracker.utils.formatDateToMediumPatternString
import com.josephlimbert.weighttracker.utils.formatMillisToDateString
import com.josephlimbert.weighttracker.utils.formatStringToTimestamp
import com.josephlimbert.weighttracker.ui.shared.LoadingIndicator
import com.josephlimbert.weighttracker.ui.theme.WeightTrackerTheme
import kotlinx.serialization.Serializable
import java.time.LocalDate
import java.util.Date

@Serializable
data class AddWeight(val weightId: String? = null) : NavKey

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddWeightSheet(onDismiss: () -> Unit, weightId: String? = null, viewModel: AddWeightViewModel = hiltViewModel()) {
    val weight = remember { mutableStateOf<Weight?>(null) }
    val user = viewModel.user.collectAsStateWithLifecycle(null)

    if (weight.value != null) {
        AddWeightSheetContent(
            onSubmit = { newWeight ->
                viewModel.addWeight(user.value!!.id, newWeight)
                onDismiss()
            },
            selectedWeight = weight.value!!,
            weightUnit = user.value?.weightUnit ?: "lbs"
        )
    } else {
        LoadingIndicator()
    }

    LaunchedEffect(Unit) {
        weight.value = viewModel.getWeight(weightId)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddWeightSheetContent(onSubmit: (weight: Weight) -> Unit, selectedWeight: Weight, weightUnit: String) {
    val weightState = rememberTextFieldState(selectedWeight.weight.toString())
    var selectedDate by remember { mutableStateOf(formatDateToMediumPatternString(Date())) }
    var showModal by remember { mutableStateOf(false) }
    val titleText = if (selectedWeight.id.isNotBlank()) stringResource(R.string.edit_weight_label) else stringResource(R.string.add_new_weight)

    if (selectedWeight.id.isNotBlank()) {
        selectedDate = formatDateToMediumPatternString(selectedWeight.recordedDate.toDate())
    }

        Column(modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = titleText, style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.W500)
            OutlinedTextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 15.dp, start = 15.dp, end = 15.dp, bottom = 10.dp),
                state = weightState,
                label = { Text("Weight") },
                trailingIcon = {
                    Text(weightUnit)
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
            )
            OutlinedTextField(
                value = selectedDate,
                onValueChange = { },
                label = { Text("Date") },
                placeholder = { Text("MM/DD/YYYY") },
                readOnly = true,
                trailingIcon = {
                    Icon(
                        painterResource(R.drawable.calendar_icon),
                        contentDescription = "Select date"
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 15.dp, end = 15.dp)
                    .pointerInput(selectedDate) {
                        awaitEachGesture {
                            awaitFirstDown(pass = PointerEventPass.Initial)
                            val upEvent = waitForUpOrCancellation(pass = PointerEventPass.Initial)
                            if (upEvent != null) {
                                showModal = true
                            }
                        }
                    }
            )

            if (showModal) {
                DatePickerModal(onDateSelected = {
                    selectedDate = formatMillisToDateString(it!!)
                                                 Log.d("DATE", it.toString())
                                                 }, onDismiss = { showModal = false })
            }
            Button(onClick = {

                onSubmit(selectedWeight.copy(weight = weightState.text.toString().toDouble(), recordedDate = formatStringToTimestamp(selectedDate)))
            }, modifier = Modifier.padding(top = 50.dp)) {
                Text("Submit")
            }
        }
}

@Composable
fun DatePickerModal(
    onDateSelected: (Long?) -> Unit,
    onDismiss: () -> Unit
) {
    val datePickerState = rememberDatePickerState(initialSelectedDate = LocalDate.now())

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                onDateSelected(datePickerState.selectedDateMillis)
                onDismiss()
            }) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    ) {
        DatePicker(state = datePickerState)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
@Preview(showSystemUi = false)
fun AddWeightSheetPreview() {
    WeightTrackerTheme{
        AddWeightSheetContent(onSubmit = { _ -> }, selectedWeight = Weight(), weightUnit = "lbs")
    }
}