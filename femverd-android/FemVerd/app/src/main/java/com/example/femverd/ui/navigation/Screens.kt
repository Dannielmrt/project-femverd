package com.example.femverd.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.FormatListBulleted
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

sealed class BottomNavItem(val route: String, val title: String, val icon: ImageVector) {
    object Home : BottomNavItem("home", "Home", Icons.Default.Home)
    object History : BottomNavItem("history", "History",
        Icons.AutoMirrored.Filled.FormatListBulleted
    )
    object Map : BottomNavItem("map", "Map", Icons.Default.Place)
}

sealed class DrawerItem(val route: String, val title: String, val icon: ImageVector) {
    object Profile : DrawerItem("profile", "My profile", Icons.Default.Person)
    object Certificate : DrawerItem("certificate", "Tax Certificate", Icons.Default.Description)
    object Help : DrawerItem("help", "Help", Icons.Default.Help)
}

object Rewards : BottomNavItem("rewards", "Rewards", Icons.Default.CardGiftcard)