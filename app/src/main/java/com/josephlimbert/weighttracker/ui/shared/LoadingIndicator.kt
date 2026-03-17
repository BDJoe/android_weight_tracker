package com.josephlimbert.weighttracker.ui.shared

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun LoadingIndicator(modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize()) {

        CircularProgressIndicator(
            modifier = Modifier
                .width(64.dp)
                .align(Alignment.Center),
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            trackColor = MaterialTheme.colorScheme.onSecondaryContainer,
        )
    }
}