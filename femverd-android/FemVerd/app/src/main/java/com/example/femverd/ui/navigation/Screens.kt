package com.example.femverd.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

sealed class BottomNavItem(val route: String, val title: String, val icon: ImageVector) {
    object Home : BottomNavItem("home", "Inicio", Icons.Default.Home)
    object History : BottomNavItem("history", "Historial", Icons.Default.FormatListBulleted)
    object Map : BottomNavItem("map", "Mapa", Icons.Default.Place)
}

sealed class DrawerItem(val route: String, val title: String, val icon: ImageVector) {
    object Profile : DrawerItem("profile", "Mi Perfil", Icons.Default.Person)
    object Certificate : DrawerItem("certificate", "Certificado Fiscal", Icons.Default.Description)
    object Help : DrawerItem("help", "Ayuda", Icons.Default.Help)
}