package com.example.glamora.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.glamora.ui.screens.home.HomeScreen
import androidx.compose.material3.Text
import com.example.glamora.ui.screen.*

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Login : Screen("login")
    object Register : Screen("register")
    object Home : Screen("home")
    object Discover : Screen("discover")
    object Cart : Screen("cart")
    object Profile : Screen("profile")
    object ProductDetail : Screen("productDetail")
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
        composable(Screen.Cart.route) {
            CartScreen(navController)
        }
        composable(Screen.Profile.route) {
            ProfileScreen(navController)
        }
        composable(Screen.ProductDetail.route) {
            ProductDetailScreen(navController)
        }
    }
}


@Composable
fun LoginScreen(navController: NavHostController) {
    Text("Login Screen")
}

@Composable
fun RegisterScreen(navController: NavHostController) {
    Text("Register Screen")
}

@Composable
fun DiscoverScreen(navController: NavHostController) {
    Text("Discover Screen")
}

@Composable
fun CartScreen(navController: NavHostController) {
    Text("Cart Screen")
}

@Composable
fun ProfileScreen(navController: NavHostController) {
    Text("Profile Screen")
}

@Composable
fun ProductDetailScreen(navController: NavHostController) {
    Text("Product Detail Screen")
}
