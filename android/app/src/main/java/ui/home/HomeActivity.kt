package com.caas.app.ui.home

import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import androidx.navigation.fragment.NavHostFragment
import com.caas.app.R
import com.caas.app.core.notifications.StockAlertNotificationHelper
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth

class HomeActivity : AppCompatActivity() {

    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser == null) {
            finish()
            return
        }

        StockAlertNotificationHelper.createNotificationChannel(this)
        requestNotificationPermissionIfNeeded()

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment_home) as NavHostFragment
        navController = navHostFragment.navController

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNavigation)
        setupBottomNavigation(bottomNav)
    }

    private fun setupBottomNavigation(bottomNav: BottomNavigationView) {
        val topLevelIds = setOf(
            R.id.homeFragment,
            R.id.businessListFragment,
            R.id.reportsFragment,
            R.id.profileFragment
        )

        // Sync bottom nav highlight with current destination
        navController.addOnDestinationChangedListener { _, destination, _ ->
            if (destination.id in topLevelIds) {
                bottomNav.menu.findItem(destination.id)?.isChecked = true
            }
        }

        bottomNav.setOnItemSelectedListener { item ->
            val currentId = navController.currentDestination?.id
            if (currentId == item.itemId) {
                // Same tab tapped: pop to root of this tab
                navController.popBackStack(item.itemId, false)
            } else {
                navController.navigate(
                    item.itemId,
                    null,
                    NavOptions.Builder()
                        .setLaunchSingleTop(true)
                        .setRestoreState(true)
                        .setPopUpTo(
                            navController.graph.startDestinationId,
                            inclusive = false,
                            saveState = true
                        )
                        .build()
                )
            }
            true
        }

        // Tapping same tab when already at root: no action needed
        bottomNav.setOnItemReselectedListener { }
    }

    private fun requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissions(
                    arrayOf(android.Manifest.permission.POST_NOTIFICATIONS),
                    NOTIFICATION_PERMISSION_REQUEST_CODE
                )
            }
        }
    }

    companion object {
        private const val NOTIFICATION_PERMISSION_REQUEST_CODE = 1001
    }
}
