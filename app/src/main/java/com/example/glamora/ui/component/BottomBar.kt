package com.example.glamora.ui.component

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavController
import com.example.glamora.ui.navigation.Screen

data class NavItem(val screen: Screen, val icon: ImageVector, val label: String)

@Composable
fun BottomBar(navController: NavController) {
    val items = listOf(
        NavItem(Screen.Home, Icons.Default.Home, "Home"),
        NavItem(Screen.Discover, Icons.Default.Search, "Discover"),
        NavItem(Screen.Cart, Icons.Default.ShoppingCart, "Cart"),
        NavItem(Screen.Profile, Icons.Default.Person, "Profile")
    )

    NavigationBar {
        items.forEach { item ->
            NavigationBarItem(
                icon = { Icon(item.icon, contentDescription = item.label) },
                label = { Text(item.label) },
                selected = false,
                onClick = {
                    navController.navigate(item.screen.route) {
                        popUpTo(Screen.Home.route)
                        launchSingleTop = true
                    }
                }
            )
        }
    }
}
