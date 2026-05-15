package com.example.femverd.ui.navigation

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.femverd.ui.screens.auth.LoginScreen
import com.example.femverd.ui.screens.home.HomeScreen
import kotlinx.coroutines.launch

@Composable
fun FemVerdApp() {
    val navController = rememberNavController()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    // rememberSaveable saves the current path so that it survives screen rotation
    var currentRoute by rememberSaveable { mutableStateOf(BottomNavItem.Home.route) }

    // The actual navigation status is monitored for synchronization
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val actualRoute = navBackStackEntry?.destination?.route ?: currentRoute

    val bottomNavItems = listOf(
        BottomNavItem.Home,
        BottomNavItem.History,
        BottomNavItem.Map
    )

    val drawerItems = listOf(
        DrawerItem.Profile,
        DrawerItem.Certificate,
        DrawerItem.Help
    )

    // NavigationDrawer
    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = true,
        drawerContent = {
            ModalDrawerSheet {
                Text(
                    text = "FemVerd",
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.padding(16.dp)
                )
                Divider()
                Spacer(modifier = Modifier.height(8.dp))

                drawerItems.forEach { item ->
                    NavigationDrawerItem(
                        icon = { Icon(item.icon, contentDescription = item.title) },
                        label = { Text(item.title) },
                        selected = actualRoute == item.route,
                        onClick = {
                            currentRoute = item.route
                            navController.navigate(item.route) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                            scope.launch { drawerState.close() } // Close the menu onClick
                        },
                        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                    )
                }
            }
        }
    ) {
        // Scaffold
        Scaffold(
            bottomBar = {
                // The bottom bar is only displayed if we are in Home, History or Map.
                if (bottomNavItems.any { it.route == actualRoute }) {
                    NavigationBar(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                    ) {
                        bottomNavItems.forEach { item ->
                            NavigationBarItem(
                                icon = { Icon(item.icon, contentDescription = item.title) },
                                label = { Text(item.title) },
                                selected = actualRoute == item.route,
                                onClick = {
                                    currentRoute = item.route
                                    navController.navigate(item.route) {
                                        popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                },
                                colors = NavigationBarItemDefaults.colors(
                                    indicatorColor = MaterialTheme.colorScheme.secondaryContainer,
                                    selectedIconColor = MaterialTheme.colorScheme.onSecondaryContainer,
                                    selectedTextColor = MaterialTheme.colorScheme.onSurface,
                                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )
                        }
                    }
                }
            }
        ) { innerPadding ->

            // NavHost
            NavHost(
                navController = navController,
                startDestination = "login",
                modifier = Modifier.padding(innerPadding)
            ) {
                composable("login") {
                    LoginScreen(
                        onLoginSuccess = {
                            navController.navigate(BottomNavItem.Home.route) {
                                popUpTo("login") { inclusive = true }
                            }
                        }
                    )
                }
                composable(BottomNavItem.Home.route) {
                    HomeScreen()
                }
                composable(BottomNavItem.History.route) { Text("History Screen", Modifier.padding(16.dp)) }
                composable(BottomNavItem.Map.route) { Text("Map Screen", Modifier.padding(16.dp)) }

                composable(DrawerItem.Profile.route) { Text("Profile Screen", Modifier.padding(16.dp)) }
                composable(DrawerItem.Certificate.route) { Text("Certificate Screen", Modifier.padding(16.dp)) }
                composable(DrawerItem.Help.route) { Text("Help Screen", Modifier.padding(16.dp)) }
                /*
                composable(BottomNavItem.Home.route) {
                    // HomeScreen.kt
                    HomeScreen()
                }
                 */

            }
        }
    }
}