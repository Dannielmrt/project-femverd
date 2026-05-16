package com.example.femverd.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.FormatListBulleted
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.femverd.R

sealed class BottomNavItem(val route: String, val titleResId: Int, val icon: ImageVector) {
    object Home : BottomNavItem("home", R.string.nav_home, Icons.Default.Home)
    object History :
        BottomNavItem("history", R.string.nav_history, Icons.AutoMirrored.Filled.FormatListBulleted)

    object Map : BottomNavItem("map", R.string.nav_map, Icons.Default.Place)
}

sealed class DrawerItem(val route: String, val titleResId: Int, val icon: ImageVector) {
    object Profile : DrawerItem("profile", R.string.nav_profile, Icons.Default.Person)
    object Certificate :
        DrawerItem("certificate", R.string.nav_certificate, Icons.Default.Description)

    object Help : DrawerItem("help", R.string.nav_help, Icons.Default.Help)
}

object Rewards : BottomNavItem("rewards", R.string.nav_rewards, Icons.Default.CardGiftcard)