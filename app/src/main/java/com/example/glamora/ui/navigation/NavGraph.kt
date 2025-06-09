package com.example.glamora.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.glamora.ui.screen.CartScreen
import com.example.glamora.ui.screen.LoginScreen
import com.example.glamora.ui.screen.RegisterScreen
import com.example.glamora.ui.screen.SplashScreen
import com.example.glamora.ui.screens.DiscoverScreen
import com.example.glamora.ui.screens.ProductDetailScreen
import com.example.glamora.ui.screens.ProfileScreen
import com.example.glamora.ui.screens.home.HomeScreen


sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Login : Screen("login")
    object Register : Screen("register")
    object Home : Screen("home")
    object Discover : Screen("discover")
    object Cart : Screen("cart")
    object Profile : Screen("profile")
    object ProductDetail : Screen("productDetail")  // Removed parameter from route
}

@Composable
fun GlamoraNavGraph(navController: NavHostController = rememberNavController()) {
    NavHost(navController = navController, startDestination = Screen.Splash.route) {
        composable(Screen.Splash.route) {
            SplashScreen(navController)
        }
        composable(Screen.Login.route) {
            LoginScreen(navController)
        }
        composable(Screen.Register.route) {
            RegisterScreen(navController)
        }
        composable(Screen.Home.route) {
            HomeScreen(navController)
        }
        composable(Screen.Discover.route) {
            DiscoverScreen(navController)
        }
        composable(Screen.ProductDetail.route) {
            ProductDetailScreen(navController)
        }
        composable(Screen.Cart.route) {
            CartScreen(navController)
        }
        composable(Screen.Profile.route) {
            ProfileScreen(navController)
        }

    }
}


