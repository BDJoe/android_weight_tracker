package com.josephlimbert.weighttracker

import android.view.Gravity
import android.view.WindowManager
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxDefaults
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.DialogWindowProvider

// Source - https://stackoverflow.com/a/79611497
// Posted by Miguel Garcia, modified by community. See post 'Timeline' for change history
// Retrieved 2026-03-20, License - CC BY-SA 4.0


@Composable
fun AppSnackbarHost(
    snackbarHostState: SnackbarHostState,
    modifier: Modifier = Modifier
) {
    val dismissState = rememberSwipeToDismissBoxState(
        SwipeToDismissBoxValue.Settled,
        SwipeToDismissBoxDefaults.positionalThreshold
    )

    LaunchedEffect(snackbarHostState.currentSnackbarData) {
        snackbarHostState.currentSnackbarData?.let {
            dismissState.reset()
        }
    }

    snackbarHostState.currentSnackbarData
        ?.takeIf { it.visuals.message.isNotEmpty() }
        ?.let { data ->
            Dialog(
                onDismissRequest = {
                    data.dismiss()
                },
                properties = DialogProperties(
                    dismissOnClickOutside = true,
                    dismissOnBackPress = true,
                    usePlatformDefaultWidth = false
                )
            ) {
                // Position the dialog at the bottom of the screen
                (LocalView.current.parent as DialogWindowProvider).window.apply {
                    setGravity(Gravity.BOTTOM)
                    clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)  // Remove the dim background
                    // Don't add FLAG_NOT_TOUCHABLE so swipe gestures work
                    // Don't add FLAG_NOT_FOCUSABLE so clicks outside can dismiss

                    // Set layout parameters to position at bottom with proper width
                    attributes = attributes.apply {
                        width = WindowManager.LayoutParams.MATCH_PARENT
                        // No need to set height as it will wrap content
                    }
                }

                val density = LocalDensity.current
                val navigationBarPadding = with(density) {
                    WindowInsets.navigationBars.getBottom(this).toDp()
                }

                SwipeToDismissBox(
                    modifier = modifier.padding(bottom = navigationBarPadding),
                    state = dismissState,
                    backgroundContent = {}
                ) {
                    SnackbarHost(hostState = snackbarHostState) {
                        Snackbar(
                            snackbarData = it,
                            actionOnNewLine = true
                        )
                    }
                }
            }
        }
}
