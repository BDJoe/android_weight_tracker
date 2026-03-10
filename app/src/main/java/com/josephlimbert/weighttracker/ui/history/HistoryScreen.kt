package com.josephlimbert.weighttracker.ui.history

import androidx.compose.foundation.Image
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
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedIconButton
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
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.josephlimbert.weighttracker.R
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.compose.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.compose.cartesian.data.lineSeries
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import kotlin.math.exp

@Composable
fun HistoryScreen() {
    HistoryScreenContent()
}

@Composable
fun HistoryScreenContent() {
    Scaffold() { innerPadding ->
        Column(modifier = Modifier
            .fillMaxSize()
            .padding(
                top = innerPadding.calculateTopPadding(),
                bottom = 4.dp,
                start = 4.dp,
                end = 4.dp
            )) {
            HistoryChart()
            LazyColumn() {
                items(3) {
                    ListItem()
                }
            }
        }
    }
}

@Composable
fun ListItem() {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = "Feb 26", style = MaterialTheme.typography.titleLarge, modifier = Modifier
                .padding(start = 8.dp, end = 10.dp, top = 8.dp, bottom = 8.dp))
            HorizontalDivider()
        }

        OutlinedCard(modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier
                    .width(60.dp)
                    .background(colorResource(R.color.md_theme_secondaryContainer))
                    .padding(vertical = 5.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "Fri", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.W500)
                    Text(text = "6", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.W500)
                }

                Text(text = "240 lbs", style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(start = 15.dp, end = 30.dp))

                Icon(painterResource(R.drawable.down_arrow_icon), "Weight gain/loss icon", tint = colorResource(R.color.md_theme_success))

                Text(text = "-3 lbs", style = MaterialTheme.typography.bodyLarge,
                    color = colorResource(R.color.md_theme_success),
                    modifier = Modifier.padding(start = 5.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    DropdownMenu(modifier = Modifier.padding(end = 10.dp))
                }

            }
        }
    }
}

@Composable
fun DropdownMenu(modifier: Modifier) {
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = modifier) {
        OutlinedIconButton(onClick = {expanded = !expanded}) {
            Icon(painterResource(R.drawable.menu_icon), "menu icon")
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = {}
        ) {
            DropdownMenuItem(text = { Text(stringResource(R.string.edit_weight_label)) }, onClick = {})
            DropdownMenuItem(text = { Text(stringResource(R.string.delete_entry)) }, onClick = {})
        }
    }
}

@Composable
fun HistoryChart(modifier: Modifier = Modifier) {
    val modelProducer = remember { CartesianChartModelProducer() }
    LaunchedEffect(Unit) {
        modelProducer.runTransaction {
            lineSeries { series(13, 8, 7, 12, 0, 1, 15, 14, 0, 11, 6, 12, 0, 11, 12, 11) }
        }
    }
    CartesianChartHost(
        chart =
            rememberCartesianChart(
                rememberLineCartesianLayer(),
                startAxis = VerticalAxis.rememberStart(),
                bottomAxis = HorizontalAxis.rememberBottom(),
            ),
        modelProducer = modelProducer,
        modifier = modifier,
    )
}

@Composable
@Preview(showSystemUi = true)
fun HistoryScreenPreview() {
    HistoryScreen()
}