package com.josephlimbert.weighttracker.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.graphics.rotationMatrix
import com.josephlimbert.weighttracker.R

@Composable
fun HomeScreen() {
    HomeScreenContent()
}

@Composable
fun HomeScreenContent() {
    val scrollState = rememberScrollState()
    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = {}) {
                Icon(painterResource(R.drawable.add_icon), stringResource(R.string.add_weight))
            }
        }
    ) { innerPadding->
        Column(
            modifier = Modifier
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
            CurrentWeightCard()
            StatsCard()
        }
    }
}

@Composable
fun CurrentWeightCard() {
    OutlinedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
    ) {
        Row(modifier = Modifier.fillMaxSize(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceEvenly) {
            Column() {
                Text(text = stringResource(R.string.start_weight),
                    modifier = Modifier.padding(bottom = 5.dp),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center)
                Text("250 lbs", style = MaterialTheme.typography.titleMedium)
            }

            Box() {
                CircularProgressIndicator(
                    modifier = Modifier
                        .padding(vertical = 20.dp)
                        .size(200.dp)
                        .rotate(-90F),
                    strokeWidth = 10.dp,
                    gapSize = 0.dp,
                    progress = { 0.5F }
                )
                Column(
                    modifier = Modifier.matchParentSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(text = "15%", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.W500)
                    Text(text = "Current Weight", style = MaterialTheme.typography.headlineSmall)
                    Text(text = "230 lbs", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.W500)
                }
            }

            Column() {
                Text(text = stringResource(R.string.goal_weight),
                    modifier = Modifier.padding(bottom = 5.dp),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center)
                Text("175 lbs", style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}

@Composable
fun StatsCard() {
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
                    Text(text = "50 lbs", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.W500)
                    Spacer(modifier = Modifier.size(10.dp))
                    Text(text = stringResource(R.string.remaining), style = MaterialTheme.typography.bodyLarge)
                    Text(text = "50 lbs", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.W500)
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = stringResource(R.string.lost_so_far), style = MaterialTheme.typography.bodyLarge)
                    Text(text = "50 lbs", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.W500)
                    Spacer(modifier = Modifier.size(10.dp))
                    Text(text = stringResource(R.string.start_date), style = MaterialTheme.typography.bodyLarge)
                    Text(text = "Feb 29, 2026", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.W500)
                }
            }
        }
    }
}

@Composable
@Preview(showSystemUi = true)
fun HomeScreenPreview() {
    MaterialTheme() {
        HomeScreenContent()
    }
}