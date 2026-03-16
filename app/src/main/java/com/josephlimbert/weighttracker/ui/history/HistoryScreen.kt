package com.josephlimbert.weighttracker.ui.history

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedIconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.NavKey
import com.josephlimbert.weighttracker.R
import com.josephlimbert.weighttracker.data.model.Weight
import com.josephlimbert.weighttracker.data.utils.formatDateToDayOfMonthString
import com.josephlimbert.weighttracker.data.utils.formatDateToDayOfWeekString
import com.josephlimbert.weighttracker.data.utils.formatDateToMonthYearString
import com.josephlimbert.weighttracker.ui.shared.LoadingIndicator
import com.josephlimbert.weighttracker.ui.sheet.AddWeightSheet
import com.josephlimbert.weighttracker.ui.theme.ExtendedColorScheme
import com.josephlimbert.weighttracker.ui.theme.extendedLight
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.compose.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianValueFormatter
import com.patrykandpatrick.vico.compose.cartesian.data.lineSeries
import com.patrykandpatrick.vico.compose.cartesian.decoration.HorizontalLine
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.compose.cartesian.rememberVicoScrollState
import com.patrykandpatrick.vico.compose.common.Fill
import com.patrykandpatrick.vico.compose.common.Insets
import com.patrykandpatrick.vico.compose.common.Position
import com.patrykandpatrick.vico.compose.common.component.TextComponent
import com.patrykandpatrick.vico.compose.common.component.rememberLineComponent
import com.patrykandpatrick.vico.compose.common.component.rememberShapeComponent
import com.patrykandpatrick.vico.compose.common.component.rememberTextComponent
import com.patrykandpatrick.vico.compose.common.data.ExtraStore
import kotlinx.serialization.Serializable
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@Serializable
data object History : NavKey

private data class ListItem(
    val weight: Weight,
    val weightDiff: Double
)

@Composable
fun HistoryScreen(modifier: Modifier, onNavigateToAddWeight: (weightId: String) -> Unit, viewModel: HistoryViewModel = hiltViewModel()) {
    val weights = viewModel.weights.collectAsStateWithLifecycle(emptyList())
    val goalWeight = viewModel.goalWeight.collectAsStateWithLifecycle(null)
    val userId by viewModel.userId.collectAsStateWithLifecycle(null)
    var weightsGrouped: Map<String, List<ListItem>> by remember { mutableStateOf(emptyMap()) }
    if (userId == null) {
        LoadingIndicator()
    } else {
        HistoryScreenContent(
            modifier = modifier,
            onNavigateToAddWeight = onNavigateToAddWeight,
            weightsGrouped,
            weights.value,
            goalWeight.value,
            viewModel::deleteWeight
        )
    }

    LaunchedEffect(weights.value) {
        val weightList = weights.value.mapIndexed { index, weight ->
            var weightDiff = 0.0
            if (index < weights.value.size - 1) {
                weightDiff = viewModel.getWeightDiff(weight.weight, weights.value[index + 1].weight)
            }
            ListItem(
                weight = weight,
                weightDiff = weightDiff)
        }
        weightsGrouped = weightList.groupBy { formatDateToMonthYearString(it.weight.recordedDate.toDate()) }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HistoryScreenContent(
    modifier: Modifier,
    onNavigateToAddWeight: (weightId: String) -> Unit,
    weightsGrouped: Map<String, List<ListItem>>,
    weights: List<Weight>,
    goalWeight: Double?,
    deleteWeight: (weightId: String) -> Unit,
) {
    var openDeleteDialog by remember {mutableStateOf(false)}
    var selectedWeightId by remember { mutableStateOf("") }

    Scaffold() { innerPadding ->
        Column(modifier = modifier
            .fillMaxSize()
            .padding(
                top = innerPadding.calculateTopPadding(),
                bottom = 4.dp,
                start = 4.dp,
                end = 4.dp
            )) {
            if (weights.isNotEmpty()) HistoryChart(weights, goalWeight)

            LazyColumn(modifier = Modifier.fillMaxSize()) {
                weightsGrouped.forEach  { (month, listItems) ->
                    stickyHeader(key = month) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = month,
                                style = MaterialTheme.typography.titleLarge,
                                modifier = Modifier
                                    .padding(start = 8.dp, end = 10.dp, top = 8.dp, bottom = 8.dp)
                            )
                            HorizontalDivider()
                        }
                    }

                    items(items = listItems, key = { listItem -> listItem.weight.id }) { listItem ->
                        ListItem(listItem,
                            onEdit = { onNavigateToAddWeight(listItem.weight.id) },
                            onDelete = {weightId ->
                                openDeleteDialog = true
                                selectedWeightId = weightId
                            })
                    }
                }
            }
        }
//        if (showBottomSheet) {
//            AddWeightSheet(
//                onDismiss = { showBottomSheet = false },
//                onSubmit = {weight ->
//                    editWeight(userId, weight)
//                    showBottomSheet = false },
//                weight = selectedWeight
//            )
//        }

        if (openDeleteDialog){
            ConfirmDeleteDialog(
                onDismissRequest = {
                    openDeleteDialog = false
                    selectedWeightId = ""
                },
                onConfirmation = {
                    deleteWeight(selectedWeightId)
                    openDeleteDialog = false
                    selectedWeightId = ""
                })
        }
    }
}

@Composable
private fun ListItem(listItem: ListItem, onEdit: () -> Unit, onDelete: (weightId: String) -> Unit) {
    val formattedDayOfWeekText = formatDateToDayOfWeekString(listItem.weight.recordedDate.toDate())
    val formattedDayText = formatDateToDayOfMonthString(listItem.weight.recordedDate.toDate())
    val formattedWeightText = listItem.weight.weight.toString() + " lbs"

    val weightDiff = when {
        (listItem.weightDiff > 0) -> "+" + listItem.weightDiff
        (listItem.weightDiff < 0) -> listItem.weightDiff.toString()
        else -> ""
    }
    val icon = when {
        (listItem.weightDiff > 0) -> ImageVector.vectorResource(R.drawable.up_arrow_icon)
        (listItem.weightDiff < 0) -> ImageVector.vectorResource(R.drawable.down_arrow_icon)
        else -> ImageVector.vectorResource(R.drawable.equal_icon)
    }
    val iconTint = when {
        (listItem.weightDiff > 0) -> MaterialTheme.colorScheme.error
        (listItem.weightDiff < 0) ->  extendedLight.success.color
        else -> MaterialTheme.colorScheme.onSurface
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        OutlinedCard(modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier
                    .width(60.dp)
                    .background(colorResource(R.color.md_theme_secondaryContainer))
                    .padding(vertical = 5.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = formattedDayOfWeekText, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.W500)
                    Text(text = formattedDayText, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.W500)
                }

                Text(text = formattedWeightText, style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(start = 15.dp, end = 30.dp))

                Icon(imageVector = icon, "Weight gain/loss icon", tint = iconTint)

                Text(text = weightDiff, style = MaterialTheme.typography.bodyLarge,
                    color = iconTint,
                    modifier = Modifier.padding(start = 5.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    DropdownMenu(modifier = Modifier.padding(end = 10.dp), onEdit = onEdit, onDelete = { onDelete(listItem.weight.id) })
                }

            }
        }
    }
}

@Composable
fun DropdownMenu(modifier: Modifier, onEdit: () -> Unit, onDelete: () -> Unit) {
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = modifier) {
        OutlinedIconButton(onClick = {expanded = !expanded}) {
            Icon(painterResource(R.drawable.menu_icon), "menu icon")
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false}
        ) {
            DropdownMenuItem(text = { Text(stringResource(R.string.edit_weight_label)) }, onClick = {
                onEdit()
                expanded = false
            })
            DropdownMenuItem(text = { Text(stringResource(R.string.delete_entry)) }, onClick = {
                onDelete()
                expanded = false
            })
        }
    }
}

@Composable
fun ConfirmDeleteDialog(
    onDismissRequest: () -> Unit,
    onConfirmation: () -> Unit,
) {
    AlertDialog(
        title = {
            Text(text = "Are You Sure?")
        },
        text = {
            Text(text = "This action is permanent and cannot be undone!")
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
fun HistoryChart(weights: List<Weight>, goalWeight: Double?) {
    val modelProducer = remember { CartesianChartModelProducer() }
    val decorations = if (goalWeight != null) listOf(rememberHorizontalLine(goalWeight)) else emptyList()
    val yValues = weights.reversed().map { weight -> weight.weight }

    LaunchedEffect(weights) {
        modelProducer.runTransaction {
            lineSeries { series(y = yValues) }
        }
    }

    CartesianChartHost(
        chart =
            rememberCartesianChart(
                rememberLineCartesianLayer(
                ),
                startAxis = VerticalAxis.rememberStart(),
                bottomAxis = HorizontalAxis.rememberBottom(label = null),
                decorations = decorations
            ),
        modelProducer = modelProducer,
        scrollState = rememberVicoScrollState(scrollEnabled = false)
    )
}
@Composable
private fun rememberHorizontalLine(goalWeight: Double): HorizontalLine {
    val fill = Fill(Color.Green)
    val line = rememberLineComponent(fill = fill, thickness = 2.dp)
    val labelComponent =
        rememberTextComponent(
            margins = Insets(start = 6.dp),
            padding = Insets(start = 8.dp, top = 2.dp, end = 8.dp, bottom = 4.dp),
            background =
                rememberShapeComponent(fill, RoundedCornerShape(bottomStart = 4.dp, bottomEnd = 4.dp)),
        )
    return remember {
        HorizontalLine(
            y = { goalWeight },
            line = line,
            labelComponent = labelComponent,
            label = { "Goal Weight" },
            verticalLabelPosition = Position.Vertical.Bottom,
        )
    }
}

@Composable
@Preview(showSystemUi = false)
fun HistoryScreenPreview() {
    HistoryScreen(modifier = Modifier, onNavigateToAddWeight = {})
}