package com.example.glamora.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.glamora.ui.screen.*

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Login : Screen("login")
    object Register : Screen("register")
    object Home : Screen("home")
    object Categories : Screen("categories")
    object Cart : Screen("cart")
    object Profile : Screen("profile")
    object ProductDetail : Screen("productDetail/{productId}") {
        fun createRoute(productId: String) = "productDetail/$productId"
    }
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
        composable(Screen.Categories.route) {
            CategoriesScreen(navController)
        }
        composable(Screen.Cart.route) {
            CartScreen(navController)
        }
        composable(Screen.Profile.route) {
            ProfileScreen(navController)
        }
        composable(Screen.ProductDetail.route) { backStackEntry ->
            val productId = backStackEntry.arguments?.getString("productId") ?: ""
            ProductDetailScreen(productId = productId, navController = navController)
        }
    }
}

@Composable
fun ProductDetailScreen(productId: String, navController: NavHostController) {
    TODO("Not yet implemented")
}

@Composable
fun ProfileScreen(x0: NavHostController) {
    TODO("Not yet implemented")
}

@Composable
fun CartScreen(x0: NavHostController) {
    TODO("Not yet implemented")
}

@Composable
fun CategoriesScreen(x0: NavHostController) {
    TODO("Not yet implemented")
}

@Composable
fun HomeScreen(x0: NavHostController) {
    TODO("Not yet implemented")
}
