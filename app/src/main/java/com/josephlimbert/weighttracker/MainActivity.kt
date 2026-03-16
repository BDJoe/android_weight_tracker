package com.josephlimbert.weighttracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.window.DialogProperties
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.scene.DialogSceneStrategy
import androidx.navigation3.ui.NavDisplay
import com.josephlimbert.weighttracker.data.model.ErrorMessage
import com.josephlimbert.weighttracker.ui.history.History
import com.josephlimbert.weighttracker.ui.history.HistoryScreen
import com.josephlimbert.weighttracker.ui.home.Home
import com.josephlimbert.weighttracker.ui.home.HomeScreen
import com.josephlimbert.weighttracker.ui.settings.Settings
import com.josephlimbert.weighttracker.ui.settings.SettingsScreen
import com.josephlimbert.weighttracker.ui.sheet.AddWeight
import com.josephlimbert.weighttracker.ui.sheet.AddWeightSheet
import com.josephlimbert.weighttracker.ui.sheet.SetGoal
import com.josephlimbert.weighttracker.ui.sheet.SetGoalSheet
import com.josephlimbert.weighttracker.ui.auth.Auth
import com.josephlimbert.weighttracker.ui.auth.AuthScreen
import com.josephlimbert.weighttracker.ui.theme.WeightTrackerTheme
import dagger.hilt.android.AndroidEntryPoint

private val TOP_LEVEL_ROUTES = mapOf<NavKey, NavBarItem>(
    Home to NavBarItem(iconId = R.drawable.home_icon, description = "Home"),
    History to NavBarItem(iconId = R.drawable.history_icon, description = "History"),
    Settings to NavBarItem(iconId = R.drawable.settings_icon, description = "Settings"),
)

data class NavBarItem(
    val iconId: Int,
    val description: String
)

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            val navigationState = rememberNavigationState(
                startRoute = Home,
                topLevelRoutes = (TOP_LEVEL_ROUTES.keys)
            )
            val bottomSheetStrategy = remember { BottomSheetSceneStrategy<NavKey>() }
            val dialogStrategy = remember { DialogSceneStrategy<NavKey>() }

            val navigator = remember { Navigator(navigationState) }

            WeightTrackerTheme {
                Scaffold(
                    topBar = {
                        CenterAlignedTopAppBar(
                            title = {
                                if  (navigationState.backStacks[navigationState.topLevelRoute]?.last() == Auth) {
                                    Text(
                                        "Weight Tracker",
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                } else {
                                    Text(
                                        TOP_LEVEL_ROUTES[navigationState.topLevelRoute]?.description ?: "Weight Tracker",
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                    )
                                }
                            },
//                            navigationIcon = {
//                                // Show the back button if not on the start destination
//                                if (navigationState.backStacks[navigationState.topLevelRoute]?.last() == Auth) {
//                                    IconButton(onClick = { navigator.goBack() }) {
//                                        Icon(
//                                            painter = painterResource(R.drawable.back_arrow_icon),
//                                            contentDescription = "Back"
//                                        )
//                                    }
//                                }
//                            },
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer,
                            ),
                        )
                    },
                    bottomBar = {
                        NavigationBar {
                            TOP_LEVEL_ROUTES.forEach { topLevelRoute ->

                                val isSelected = topLevelRoute.key == navigationState.topLevelRoute
                                NavigationBarItem(
                                    selected = isSelected,
                                    onClick = {
                                        navigator.navigate(topLevelRoute.key)
                                    },
                                    icon =
                                        {
                                            Icon(
                                                imageVector = ImageVector.vectorResource(
                                                    topLevelRoute.value.iconId
                                                ), contentDescription = null
                                            )
                                        },
                                    label = {
                                        Text(text = topLevelRoute.value.description)
                                    }
                                )
                            }
                        }
                    }
                ) { contentPadding ->
                    val modifier = Modifier.padding(contentPadding)
                    val entryProvider = entryProvider {
                        entry<Home>{
                            HomeScreen(modifier = modifier, onNavigateToAuth = {
                                navigator.navigate(Auth)
                            }, onNavigateToAddWeight = {
                                navigator.navigate(AddWeight())
                            }, onNavigateToSetGoal = {
                                navigator.navigate(SetGoal)
                            })
                        }
                        entry<History>{
                            HistoryScreen(modifier = modifier, onNavigateToAddWeight = { weightId ->
                                navigator.navigate(AddWeight(weightId))
                            })
                        }
                        entry<Settings>{
                            SettingsScreen(modifier = modifier, onNavigateToSetGoal = {
                                navigator.navigate(SetGoal)
                            }, onSignOUt = { navigator.navigate(Home)})
                        }
                        entry<Auth>(metadata = DialogSceneStrategy.dialog(DialogProperties(windowTitle = "Welcome", usePlatformDefaultWidth = false))){
                            AuthScreen(openHomeScreen = { navigator.goBack() }, showErrorSnackbar = {})
                        }
                        entry<AddWeight>(metadata = BottomSheetSceneStrategy.bottomSheet()) { key ->
                            AddWeightSheet(onDismiss = { navigator.goBack() }, weightId = key.weightId)
                        }
                        entry<SetGoal>(metadata = BottomSheetSceneStrategy.bottomSheet()) { _ ->
                            SetGoalSheet(onDismiss = { navigator.goBack()})
                        }
                    }

                    NavDisplay(
                        entries = navigationState.toEntries(entryProvider = entryProvider),
                        onBack = { navigator.goBack() },
                        sceneStrategy = bottomSheetStrategy then dialogStrategy,
                    )
                }
            }
        }
    }

    private fun getErrorMessage(error: ErrorMessage): String {
        return when (error) {
            is ErrorMessage.StringError -> error.message
            is ErrorMessage.IdError -> this@MainActivity.getString(error.message)
        }
    }
}