package com.example.femverd.ui.navigation

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.femverd.R
import com.example.femverd.ui.screens.auth.LoginScreen
import com.example.femverd.ui.screens.auth.RegisterScreen
import com.example.femverd.ui.screens.certificate.CertificateScreen
import com.example.femverd.ui.screens.help.HelpScreen
import com.example.femverd.ui.screens.history.HistoryScreen
import com.example.femverd.ui.screens.home.HomeScreen
import com.example.femverd.ui.screens.map.MapScreen
import com.example.femverd.ui.screens.profile.ProfileScreen
import com.example.femverd.ui.screens.rewards.RewardsScreen
import com.example.femverd.ui.screens.splash.SplashScreen
import kotlinx.coroutines.launch

@RequiresApi(Build.VERSION_CODES.Q)
@Composable
fun FemVerdApp() {
    val navController = rememberNavController()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    var currentRoute by rememberSaveable { mutableStateOf(BottomNavItem.Home.route) }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val actualRoute = navBackStackEntry?.destination?.route ?: currentRoute

    val lockedDrawerRoutes = listOf("splash", "login", "register", "map")
    val isDrawerEnabled = actualRoute !in lockedDrawerRoutes

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

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = isDrawerEnabled,
        drawerContent = {
            ModalDrawerSheet {
                Text(
                    text = stringResource(id = R.string.app_name),
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.padding(dimensionResource(id = R.dimen.padding_medium))
                )
                HorizontalDivider()
                Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.spacing_small)))

                drawerItems.forEach { item ->
                    NavigationDrawerItem(
                        icon = {
                            Icon(
                                item.icon,
                                contentDescription = stringResource(id = item.titleResId)
                            )
                        },
                        label = { Text(stringResource(id = item.titleResId)) },
                        selected = actualRoute == item.route,
                        onClick = {
                            currentRoute = item.route
                            navController.navigate(item.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                            scope.launch { drawerState.close() }
                        },
                        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                    )
                }
            }
        }
    ) {
        Scaffold(
            bottomBar = {
                if (bottomNavItems.any { it.route == actualRoute }) {
                    NavigationBar(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                    ) {
                        bottomNavItems.forEach { item ->
                            NavigationBarItem(
                                icon = {
                                    Icon(
                                        item.icon,
                                        contentDescription = stringResource(id = item.titleResId)
                                    )
                                },
                                label = { Text(stringResource(id = item.titleResId)) },
                                selected = actualRoute == item.route,
                                onClick = {
                                    currentRoute = item.route
                                    navController.navigate(item.route) {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
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
            NavHost(
                navController = navController,
                startDestination = "splash",
                modifier = Modifier.padding(innerPadding)
            ) {
                composable("splash") {
                    SplashScreen(
                        onNavigateToLogin = {
                            navController.navigate("login") {
                                popUpTo("splash") { inclusive = true }
                            }
                        },
                        onNavigateToHome = {
                            navController.navigate(BottomNavItem.Home.route) {
                                popUpTo("splash") { inclusive = true }
                            }
                        }
                    )
                }
                composable("login") {
                    LoginScreen(
                        onLoginSuccess = {
                            navController.navigate(BottomNavItem.Home.route) {
                                popUpTo("login") { inclusive = true }
                            }
                        },
                        onNavigateToRegister = {
                            navController.navigate("register")
                        }
                    )
                }
                composable("register") {
                    RegisterScreen(
                        onRegisterSuccess = { navController.popBackStack() },
                        onNavigateBack = { navController.popBackStack() }
                    )
                }
                composable(BottomNavItem.Home.route) {
                    HomeScreen(navController)
                }
                composable(Rewards.route) {
                    RewardsScreen(navController)
                }
                composable(BottomNavItem.History.route) {
                    HistoryScreen()
                }
                composable(BottomNavItem.Map.route) {
                    MapScreen()
                }
                composable(DrawerItem.Profile.route) {
                    ProfileScreen(navController = navController)
                }
                composable(DrawerItem.Certificate.route) {
                    CertificateScreen(navController = navController)
                }
                composable(DrawerItem.Help.route) {
                    HelpScreen(navController = navController)
                }
            }
        }
    }
}