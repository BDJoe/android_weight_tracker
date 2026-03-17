package com.josephlimbert.weighttracker.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.focus.focusModifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.NavKey
import com.josephlimbert.weighttracker.R
import com.josephlimbert.weighttracker.data.model.Weight
import com.josephlimbert.weighttracker.data.utils.formatDateToMediumPatternString
import com.josephlimbert.weighttracker.ui.shared.LoadingIndicator
import kotlinx.serialization.Serializable

@Serializable
data object Home : NavKey


@Composable
fun HomeScreen(modifier: Modifier,
               navigateToAuth: () -> Unit,
               navigateToAddWeight: () -> Unit,
               navigateToSetGoal: () -> Unit,
               viewModel: HomeViewModel = hiltViewModel()
               ) {
    var isLoadingData by remember { mutableStateOf(true) }
    val userId by viewModel.userId.collectAsStateWithLifecycle("")
    val startingWeight = viewModel.startingWeight.collectAsStateWithLifecycle(null)
    val currentWeight = viewModel.currentWeight.collectAsStateWithLifecycle(null)
    val goalWeight = viewModel.goalWeight.collectAsStateWithLifecycle(null)
    val totalLossPercent = viewModel.totalLossPercent.collectAsStateWithLifecycle(null)
    val totalLossWeight = viewModel.totalLossWeight.collectAsStateWithLifecycle(null)
    val targetLoss = viewModel.targetLoss.collectAsStateWithLifecycle(null)
    val targetLeft = viewModel.targetLeft.collectAsStateWithLifecycle(null)

    if (userId == null) {
        navigateToAuth()
    } else if (startingWeight.value == null ||
        currentWeight.value == null ||
        goalWeight.value == null ||
        totalLossPercent.value == null ||
        totalLossWeight.value == null ||
        targetLoss.value == null ||
        targetLeft.value == null) {
        LoadingIndicator(modifier = modifier)
    } else {
        HomeScreenContent(
            startingWeight = startingWeight.value,
            currentWeight = currentWeight.value,
            goalWeight = goalWeight.value,
            totalLossPercent = totalLossPercent.value,
            totalLossWeight = totalLossWeight.value,
            targetLoss = targetLoss.value,
            targetLeft = targetLeft.value,
            modifier = modifier,
            onNavigateToAuth = navigateToAuth,
            onNavigateToAddWeight = navigateToAddWeight,
            onNavigateToSetGoal = navigateToSetGoal
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreenContent(
    startingWeight: Weight?,
    currentWeight: Weight?,
    goalWeight: Double?,
    totalLossPercent: Double?,
    totalLossWeight: Double?,
    targetLoss: Double?,
    targetLeft: Double?,
    modifier: Modifier,
    onNavigateToAuth: () -> Unit,
    onNavigateToAddWeight: () -> Unit,
    onNavigateToSetGoal: () -> Unit
) {
    val scrollState = rememberScrollState()
    var showAddWeightSheet by remember { mutableStateOf(false) }
    var showSetGoalSheet by remember { mutableStateOf(false) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = onNavigateToAddWeight, modifier = modifier) {
                Icon(painterResource(R.drawable.add_icon), stringResource(R.string.add_weight))
            }
        }
    ) { innerPadding->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(
                    top = innerPadding.calculateTopPadding(),
                    start = 4.dp,
                    end = 4.dp,
                    bottom = 4.dp
                )
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CurrentWeightCard(
                startingWeight = startingWeight,
                currentWeight = currentWeight,
                goalWeight = goalWeight,
                totalLossPercent = totalLossPercent,
                onClick = onNavigateToSetGoal
            )
            StatsCard(
                totalLossWeight = totalLossWeight,
                targetLoss = targetLoss,
                targetLeft = targetLeft,
                startingWeight = startingWeight,
            )
            Button(onClick = onNavigateToAuth) {
                Text(text = "Sign In")
            }
        }

//        if (showAddWeightSheet) {
//            AddWeightSheet(onDismiss = { showAddWeightSheet = false },
//                onSubmit = { weight ->
//                    addWeight(userId!!, weight)
//                    showAddWeightSheet = false
//                })
//        }

//        if (showSetGoalSheet) {
//            SetGoalSheet(onDismiss = { showSetGoalSheet = false }, onSubmit = { weight ->
//                showSetGoalSheet = false })
//        }
    }
}

@Composable
fun CurrentWeightCard(
    startingWeight: Weight?,
    currentWeight: Weight?,
    goalWeight: Double?,
    totalLossPercent: Double?,
    onClick: () -> Unit
) {
    OutlinedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
    ) {
        Column() {
            Row(
                modifier = Modifier.fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = stringResource(R.string.start_weight),
                        modifier = Modifier.padding(bottom = 5.dp),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    Text(startingWeight?.weight.toString() + " lbs", style = MaterialTheme.typography.titleMedium)
                }

                Box() {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .padding(vertical = 20.dp)
                            .size(200.dp)
                            .rotate(-90F),
                        strokeWidth = 10.dp,
                        gapSize = 0.dp,
                        progress = { (totalLossPercent?.toFloat()?.div(100)) ?: 0.0F}
                    )
                    Column(
                        modifier = Modifier.matchParentSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = totalLossPercent?.toInt().toString() + "%",
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.W500
                        )
                        Text(
                            text = "Current Weight",
                            style = MaterialTheme.typography.headlineSmall
                        )
                        Text(
                            text = currentWeight?.weight.toString() + " lbs",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.W500
                        )
                    }
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = stringResource(R.string.goal_weight),
                        modifier = Modifier.padding(bottom = 5.dp),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    Text(goalWeight?.toString() + " lbs", style = MaterialTheme.typography.titleMedium)
                }
            }
            if (goalWeight == null || goalWeight <= 0) {
                Button(
                    onClick = onClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 10.dp, horizontal = 20.dp)
                ) {
                    Text(
                        text = stringResource(R.string.set_goal_weight),
                        fontSize = 24.sp,
                        modifier = Modifier.padding(vertical = 5.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun StatsCard(
    totalLossWeight: Double?,
    targetLoss: Double?,
    targetLeft: Double?,
    startingWeight: Weight?,
) {
    val formattedDate = if (startingWeight != null) formatDateToMediumPatternString(startingWeight.recordedDate.toDate()) else "N/A"

    OutlinedCard(modifier = Modifier
        .fillMaxWidth()
        .padding(8.dp)) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(10.dp)) {
            Text(text = stringResource(R.string.stats),
                style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.W500,
            )
            HorizontalDivider()
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp),
                horizontalArrangement = Arrangement.SpaceAround,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = stringResource(R.string.target_loss), style = MaterialTheme.typography.bodyLarge)
                    Text(text = targetLoss?.toString() + " lbs", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.W500)
                    Spacer(modifier = Modifier.size(10.dp))
                    Text(text = stringResource(R.string.remaining), style = MaterialTheme.typography.bodyLarge)
                    Text(text = targetLeft?.toString() + " lbs", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.W500)
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = stringResource(R.string.lost_so_far), style = MaterialTheme.typography.bodyLarge)
                    Text(text = totalLossWeight?.toString() + " lbs", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.W500)
                    Spacer(modifier = Modifier.size(10.dp))
                    Text(text = stringResource(R.string.start_date), style = MaterialTheme.typography.bodyLarge)
                    Text(text = formattedDate, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.W500)
                }
            }
        }
    }
}

@Composable
@Preview(showSystemUi = true)
fun HomeScreenPreview() {
    MaterialTheme() {
        HomeScreen(modifier = Modifier, navigateToAuth = {}, navigateToAddWeight = {}, navigateToSetGoal = {})
    }
}