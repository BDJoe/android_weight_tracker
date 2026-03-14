package com.josephlimbert.weighttracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheetDefaults
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.josephlimbert.weighttracker.data.model.ErrorMessage
import com.josephlimbert.weighttracker.data.utils.ResultEffect
import com.josephlimbert.weighttracker.data.utils.ResultEventBus
import com.josephlimbert.weighttracker.ui.history.History
import com.josephlimbert.weighttracker.ui.history.HistoryScreen
import com.josephlimbert.weighttracker.ui.home.Home
import com.josephlimbert.weighttracker.ui.home.HomeScreen
import com.josephlimbert.weighttracker.ui.settings.Settings
import com.josephlimbert.weighttracker.ui.settings.SettingsScreen
import com.josephlimbert.weighttracker.ui.sheet.AddWeight
import com.josephlimbert.weighttracker.ui.sheet.AddWeightSheet
import com.josephlimbert.weighttracker.ui.sheet.AddWeightViewModel
import com.josephlimbert.weighttracker.ui.sheet.SetGoal
import com.josephlimbert.weighttracker.ui.sheet.SetGoalSheet
import com.josephlimbert.weighttracker.ui.signin.Auth
import com.josephlimbert.weighttracker.ui.signin.AuthScreen
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
                            navigationIcon = {
                                // Show the back button if not on the start destination
                                if (navigationState.backStacks[navigationState.topLevelRoute]?.last() == Auth) {
                                    IconButton(onClick = { navigator.goBack() }) {
                                        Icon(
                                            painter = painterResource(R.drawable.back_arrow_icon),
                                            contentDescription = "Back"
                                        )
                                    }
                                }
                            },
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
                        entry<Auth>{
                            AuthScreen(modifier = modifier, openHomeScreen = {}, showErrorSnackbar = {})
                        }
                        entry<AddWeight>(metadata = BottomSheetSceneStrategy.bottomSheet()) { key ->
                            AddWeightSheet(onDismiss = { navigator.goBack() }, weightId = key.weightId)
                        }
                        entry<SetGoal>(metadata = BottomSheetSceneStrategy.bottomSheet()) { key ->
                            SetGoalSheet(onDismiss = { navigator.goBack()})
                        }
                    }

                    NavDisplay(
                        entries = navigationState.toEntries(entryProvider = entryProvider),
                        onBack = { navigator.goBack() },
                        sceneStrategy = bottomSheetStrategy,
                    )
                }
            }



//            val navController = rememberNavController()
//            val scope = rememberCoroutineScope()
//            val snackbarHostState = remember { SnackbarHostState() }
//            val startDestination = Destination.HOME
//            var selectedDestination by rememberSaveable { mutableIntStateOf(startDestination.ordinal) }
//            val navBackStackEntry by navController.currentBackStackEntryAsState()
//            val currentDestination = navBackStackEntry?.destination?.route
//
//            WeightTrackerTheme {
//                Scaffold(
//                    modifier = Modifier.fillMaxSize(),
//                    snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
//                    topBar = {
//                        CenterTopAppBar(navController = navController, currentDestination = currentDestination)
//                    },
//                    bottomBar = {
//                        NavigationBar() {
//                            Destination.entries.forEachIndexed { index, destination ->
//                                if (destination == Destination.AUTH) return@forEachIndexed
//                                NavigationBarItem(
//                                    selected = selectedDestination == index,
//                                    onClick = {
//                                        navController.navigate(route = destination.route)
//                                        selectedDestination = index
//                                    },
//                                    icon = {
//                                        Icon(
//                                            painterResource(destination.iconId),
//                                            contentDescription = destination.contentDescription
//                                        )
//                                    },
//                                    label = { Text(destination.label) }
//                                )
//                            }
//                        }
//                    }
//                ) { contentPadding ->
//                    AppNavHost(
//                        navController,
//                        startDestination,
//                        modifier = Modifier.padding(contentPadding),
//                        scope,
//                        snackbarHostState,
//                        { error -> getErrorMessage(error)}
//                    )
//                }
//            }
        }
    }

    private fun getErrorMessage(error: ErrorMessage): String {
        return when (error) {
            is ErrorMessage.StringError -> error.message
            is ErrorMessage.IdError -> this@MainActivity.getString(error.message)
        }
    }
}

class TopLevelBackStack<T: Any>(startKey: T) {

    // Maintain a stack for each top level route
    private var topLevelStacks : LinkedHashMap<T, SnapshotStateList<T>> = linkedMapOf(
        startKey to mutableStateListOf(startKey)
    )

    // Expose the current top level route for consumers
    var topLevelKey by mutableStateOf(startKey)
        private set

    // Expose the back stack so it can be rendered by the NavDisplay
    val backStack = mutableStateListOf(startKey)

    private fun updateBackStack() =
        backStack.apply {
            clear()
            addAll(topLevelStacks.flatMap { it.value })
        }

    fun addTopLevel(key: T){

        // If the top level doesn't exist, add it
        if (topLevelStacks[key] == null){
            topLevelStacks.put(key, mutableStateListOf(key))
        } else {
            // Otherwise just move it to the end of the stacks
            topLevelStacks.apply {
                remove(key)?.let {
                    put(key, it)
                }
            }
        }
        topLevelKey = key
        updateBackStack()
    }

    fun add(key: T){
        topLevelStacks[topLevelKey]?.add(key)
        updateBackStack()
    }

    fun removeLast(){
        val removedKey = topLevelStacks[topLevelKey]?.removeLastOrNull()
        // If the removed key was a top level key, remove the associated top level stack
        topLevelStacks.remove(removedKey)
        topLevelKey = topLevelStacks.keys.last()
        updateBackStack()
    }
}

//enum class Destination(
//    val route: String,
//    val label: String,
//    val iconId: Int,
//    val contentDescription: String
//) {
//    HOME("Home", "Home", R.drawable.home_icon, "Home"),
//    HISTORY("History", "History", R.drawable.history_icon, "History"),
//    SETTINGS("Settings", "Settings", R.drawable.settings_icon, "Settings"),
//    AUTH("Sign In", "Sign In", R.drawable.ic_account_box, "Sign In")
//}
//
//@Composable
//fun AppNavHost(
//    navController: NavHostController,
//    startDestination: Destination,
//    modifier: Modifier,
//    scope: CoroutineScope,
//    snackbarHostState: SnackbarHostState,
//    getErrorMessage: (error: ErrorMessage) -> String
//) {
//    NavHost(
//        navController,
//        startDestination = startDestination.route
//    ) {
//        Destination.entries.forEach { destination ->
//            composable(destination.route) {
//                when (destination) {
//                    Destination.HOME -> HomeScreen(modifier = modifier, onNavigateToAuth = {
//                        navController.navigate(
//                            route = Destination.AUTH.route
//                        )
//                    })
//                    Destination.HISTORY -> HistoryScreen(modifier = modifier)
//                    Destination.SETTINGS -> SettingsScreen(modifier = modifier)
//                    Destination.AUTH -> AddWeightSheet(onDismiss = {}, onSubmit = {})
//                }
//            }
//        }
//    }
//}