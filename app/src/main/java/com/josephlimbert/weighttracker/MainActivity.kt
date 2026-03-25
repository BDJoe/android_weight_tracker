package com.josephlimbert.weighttracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteDefaults
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffoldDefaults
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteType
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.window.DialogProperties
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.scene.DialogSceneStrategy
import androidx.navigation3.ui.NavDisplay
import androidx.window.core.layout.WindowSizeClass.Companion.WIDTH_DP_EXPANDED_LOWER_BOUND
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
import kotlinx.coroutines.launch

private val TOP_LEVEL_ROUTES = mapOf(
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
            val scope = rememberCoroutineScope()
            val snackbarHostState = remember { SnackbarHostState() }
            val navigationState = rememberNavigationState(
                startRoute = Home,
                topLevelRoutes = TOP_LEVEL_ROUTES.keys
            )
            val bottomSheetStrategy = remember { BottomSheetSceneStrategy<NavKey>() }
            val dialogStrategy = remember { DialogSceneStrategy<NavKey>() }

            val navigator = remember { Navigator(navigationState) }

            val adaptiveInfo = currentWindowAdaptiveInfo()
            val customNavSuiteType = with(adaptiveInfo) {
                if (windowSizeClass.isWidthAtLeastBreakpoint(WIDTH_DP_EXPANDED_LOWER_BOUND)) {
                    NavigationSuiteType.WideNavigationRailCollapsed
                } else {
                    NavigationSuiteScaffoldDefaults.calculateFromAdaptiveInfo(adaptiveInfo)
                }
            }

            val showTopAppBar = with(adaptiveInfo) {
                if (windowSizeClass.isWidthAtLeastBreakpoint(WIDTH_DP_EXPANDED_LOWER_BOUND)) {
                    false
                } else {
                    true
                }
            }

            WeightTrackerTheme {
                NavigationSuiteScaffold(
                    navigationSuiteItems = {
                        TOP_LEVEL_ROUTES.entries.forEach {
                            item(
                                icon = {
                                    Icon(
                                        ImageVector.vectorResource(it.value.iconId),
                                        contentDescription = it.value.description
                                    )
                                },
                                label = { Text(it.value.description) },
                                selected = it.key == navigationState.topLevelRoute,
                                onClick = { navigator.navigate(it.key) }
                            )
                        }
                    },
                    layoutType = customNavSuiteType
                ) {

                    Scaffold(
                        topBar = {
                            if (showTopAppBar) {
                                CenterAlignedTopAppBar(
                                    title = {
                                        Text(
                                            TOP_LEVEL_ROUTES[navigationState.topLevelRoute]?.description
                                                ?: "Weight Tracker",
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            color = MaterialTheme.colorScheme.onPrimary
                                        )
                                    },
                                    colors = TopAppBarDefaults.topAppBarColors(
                                        containerColor = MaterialTheme.colorScheme.primary,
                                    ),
                                )
                            }
                        },
//                        bottomBar = {
//                            NavigationBar {
//                                TOP_LEVEL_ROUTES.forEach { topLevelRoute ->
//
//                                    val isSelected =
//                                        topLevelRoute.key == navigationState.topLevelRoute
//                                    NavigationBarItem(
//                                        selected = isSelected,
//                                        onClick = {
//                                            navigator.navigate(topLevelRoute.key)
//                                        },
//                                        icon =
//                                            {
//                                                Icon(
//                                                    imageVector = ImageVector.vectorResource(
//                                                        topLevelRoute.value.iconId
//                                                    ), contentDescription = null
//                                                )
//                                            },
//                                        label = {
//                                            Text(text = topLevelRoute.value.description)
//                                        }
//                                    )
//                                }
//                            }
//                        },
                        snackbarHost = { AppSnackbarHost(snackbarHostState = snackbarHostState) }
                    ) { contentPadding ->
                        val modifier = Modifier.padding(contentPadding)
                        val entryProvider = entryProvider {
                            entry<Home> {
                                HomeScreen(modifier = modifier, navigateToAuth = {
                                    navigator.navigate(Auth(false))
                                }, navigateToAddWeight = {
                                    navigator.navigate(AddWeight())
                                }, navigateToSetGoal = {
                                    navigator.navigate(SetGoal)
                                })
                            }
                            entry<History> {
                                HistoryScreen(
                                    modifier = modifier,
                                    navigateToAuth = { navigator.navigate(Auth(false)) },
                                    onNavigateToAddWeight = { weightId ->
                                        navigator.navigate(AddWeight(weightId))
                                    })
                            }
                            entry<Settings> {
                                SettingsScreen(
                                    modifier = modifier,
                                    onNavigateToAuth = { navigator.navigate(Auth(true)) },
                                    onNavigateToSetGoal = {
                                        navigator.navigate(SetGoal)
                                    }, onSignOUt = { navigator.navigate(Home) })
                            }
                            entry<Auth>(
                                metadata = DialogSceneStrategy.dialog(
                                    DialogProperties(
                                        windowTitle = "Welcome",
                                        usePlatformDefaultWidth = false
                                    )
                                )
                            ) { key ->
                                AuthScreen(
                                    openHomeScreen = {
                                        navigator.goBack()
                                        navigator.navigate(Home)
                                    },
                                    showErrorSnackbar = { errorMessage ->
                                        scope.launch { snackbarHostState.showSnackbar(errorMessage) }
                                    },
                                    closeAuth = { navigator.goBack() },
                                    isGuest = key.isGuest
                                )
                            }
                            entry<AddWeight>(metadata = BottomSheetSceneStrategy.bottomSheet()) { key ->
                                AddWeightSheet(
                                    onDismiss = { navigator.goBack() },
                                    weightId = key.weightId
                                )
                            }
                            entry<SetGoal>(metadata = BottomSheetSceneStrategy.bottomSheet()) { _ ->
                                SetGoalSheet(onDismiss = { navigator.goBack() })
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
    }
}