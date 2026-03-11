package com.josephlimbert.weighttracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import dagger.hilt.android.AndroidEntryPoint


class MainActivityOLD : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

//
//        // Initiate bottom navigation bar
//        val navView = findViewById<BottomNavigationView>(R.id.bottom_nav)
//        val navHostFragment = supportFragmentManager
//            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment?
//
//        if (navHostFragment != null) {
//            val navController = navHostFragment.navController
//
//            val appBarConfig = AppBarConfiguration.Builder(
//                R.id.navigation_home, R.id.navigation_history, R.id.navigation_settings
//            )
//                .build()
//            NavigationUI.setupActionBarWithNavController(this, navController, appBarConfig)
//            NavigationUI.setupWithNavController(navView, navController)
//        }
    }

//    override fun onSupportNavigateUp(): Boolean {
//        val navController = this.findNavController(R.id.nav_host_fragment)
//        return navController.navigateUp() || super.onSupportNavigateUp()
//    }
}