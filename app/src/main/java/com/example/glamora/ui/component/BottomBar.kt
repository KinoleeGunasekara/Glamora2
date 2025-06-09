package com.example.glamora.ui.component

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
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

    // Track the current route to highlight the selected item
    val currentDestination = navController.currentDestination?.route
    NavigationBar(
        tonalElevation = 8.dp,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        items.forEach { item ->
            val selected = currentDestination == item.screen.route

            // Animate the icon color when selected
            val iconColor by animateColorAsState(
                targetValue = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                animationSpec = tween(durationMillis = 300)
            )

            // Animate scale for the icon
            val scale by animateFloatAsState(
                targetValue = if (selected) 1.2f else 1f,
                animationSpec = tween(durationMillis = 300)
            )

            NavigationBarItem(
                icon = {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.label,
                        tint = iconColor,
                        modifier = androidx.compose.ui.Modifier
                            .size(24.dp)
                            .graphicsLayer {
                                scaleX = scale
                                scaleY = scale
                            }
                    )
                },
                label = {
                    Text(
                        item.label,
                        color = iconColor
                    )
                },
                selected = selected,
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