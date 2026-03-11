package com.josephlimbert.weighttracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.vectorResource
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.josephlimbert.weighttracker.ui.history.HistoryRoute
import com.josephlimbert.weighttracker.ui.history.HistoryScreen
import com.josephlimbert.weighttracker.ui.home.HomeRoute
import com.josephlimbert.weighttracker.ui.home.HomeScreen
import com.josephlimbert.weighttracker.ui.settings.SettingsRoute
import com.josephlimbert.weighttracker.ui.settings.SettingsScreen
import com.josephlimbert.weighttracker.ui.shared.CenterTopAppBar
import com.josephlimbert.weighttracker.ui.signin.AuthRoute
import com.josephlimbert.weighttracker.ui.signin.AuthScreen
import com.josephlimbert.weighttracker.ui.theme.WeightTrackerTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val navController = rememberNavController()
            val startDestination = Destination.HOME
            var selectedDestination by rememberSaveable { mutableIntStateOf(startDestination.ordinal) }
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentDestination = navBackStackEntry?.destination?.route

            WeightTrackerTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    topBar = {
                        CenterTopAppBar(navController = navController, currentDestination = currentDestination)
                    },
                    bottomBar = {
                        NavigationBar() {
                            Destination.entries.forEachIndexed { index, destination ->
                                if (destination == Destination.AUTH) return@forEachIndexed
                                NavigationBarItem(
                                    selected = selectedDestination == index,
                                    onClick = {
                                        navController.navigate(route = destination.route)
                                        selectedDestination = index
                                    },
                                    icon = {
                                        Icon(
                                            painterResource(destination.iconId),
                                            contentDescription = destination.contentDescription
                                        )
                                    },
                                    label = { Text(destination.label) }
                                )
                            }
                        }
                    }
                ) { contentPadding ->
                    AppNavHost(navController, startDestination, modifier = Modifier.padding(contentPadding))
                }
            }
        }
    }
}

enum class Destination(
    val route: String,
    val label: String,
    val iconId: Int,
    val contentDescription: String
) {
    HOME("Home", "Home", R.drawable.home_icon, "Home"),
    HISTORY("History", "History", R.drawable.history_icon, "History"),
    SETTINGS("Settings", "Settings", R.drawable.settings_icon, "Settings"),
    AUTH("Sign In", "Sign In", R.drawable.ic_account_box, "Sign In")
}

@Composable
fun AppNavHost(
    navController: NavHostController,
    startDestination: Destination,
    modifier: Modifier
) {
    NavHost(
        navController,
        startDestination = startDestination.route
    ) {
        Destination.entries.forEach { destination ->
            composable(destination.route) {
                when (destination) {
                    Destination.HOME -> HomeScreen(modifier = modifier, onNavigateToAuth = {
                        navController.navigate(
                            route = Destination.AUTH.route
                        )
                    })
                    Destination.HISTORY -> HistoryScreen(modifier = modifier)
                    Destination.SETTINGS -> SettingsScreen(modifier = modifier)
                    Destination.AUTH -> AuthScreen(modifier = modifier)
                }
            }
        }
    }
}