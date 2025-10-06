package com.example.glamora.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.room.Room

import com.example.glamora.local.AppDatabase
import com.example.glamora.repository.CartRepository
import com.example.glamora.repository.ProductRepository
import com.example.glamora.ui.screen.*
import com.example.glamora.util.NetworkMonitor // FIX: Import NetworkMonitor
import com.example.glamora.viewmodel.CartViewModel
import com.example.glamora.viewmodel.CartViewModelFactory
import com.example.glamora.viewmodel.ProductViewModel
import com.example.glamora.viewmodel.ProductViewModelFactory
import com.google.firebase.auth.FirebaseAuth

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Login : Screen("login")
    object Register : Screen("register")
    object Home : Screen("home")
    object Discover : Screen("discover")
    object Cart : Screen("cart")
    object Profile : Screen("profile")
    object ProductDetail : Screen("product_detail/{productId}") {
        fun createRoute(productId: Int) = "product_detail/$productId"
    }
    // NEW: Add the Payment screen route here
    object Payment : Screen("payment")
}

@Composable
fun GlamoraNavGraph(
    auth: FirebaseAuth,
    navController: NavHostController = rememberNavController()
) {
    val context = LocalContext.current

    // --- 0. NETWORK MONITOR ---
    // FIX: Instantiate the NetworkMonitor
    val networkMonitor = remember { NetworkMonitor(context) }

    // --- 1. ROOM DATABASE SETUP ---
    // Creates the database instance, ensuring it is built only once using remember.
    val db = remember {
        Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "glamora_db"
        ).build()
    }

    // --- 2. REPOSITORIES ---
    // Creates the DAO and Repositories, passing the DAO to CartRepository.
    val cartRepository = remember { CartRepository(db.cartDao()) }
    val productRepository = remember { ProductRepository(context) } // Uses context for assets/network check

    // --- 3. VIEWMODEL FACTORIES ---
    // FIX: Update ProductViewModelFactory to pass the NetworkMonitor
    val productViewModelFactory = remember { ProductViewModelFactory(productRepository, networkMonitor) }
    val cartViewModelFactory = remember { CartViewModelFactory(cartRepository) }

    // --- 4. VIEWMODELS ---
    // Instantiates ViewModels using the respective factories.
    val productViewModel: ProductViewModel = viewModel(factory = productViewModelFactory)
    val cartViewModel: CartViewModel = viewModel(factory = cartViewModelFactory) // The CartViewModel instance

    NavHost(navController = navController, startDestination = Screen.Home.route) {

        // Placeholder for Authentication and Splash screens
        composable(Screen.Splash.route) { SplashScreen(navController) }
        composable(Screen.Login.route) { LoginScreen(navController, auth) }
        composable(Screen.Register.route) { RegisterScreen(navController, auth) }

        // Home Screen
        composable(Screen.Home.route) {
            HomeScreen(
                navController = navController,
                viewModel = productViewModel,
                cartViewModel = cartViewModel
            )
        }

        // Discover Screen
        composable(Screen.Discover.route) {
            DiscoverScreen(
                navController = navController,
                viewModel = productViewModel,
                cartViewModel = cartViewModel
            )
        }

        // Cart Screen
        composable(Screen.Cart.route) {
            CartScreen(navController = navController, cartViewModel = cartViewModel)
        }

        // Profile Screen
        composable(Screen.Profile.route) {
            ProfileScreen(navController = navController, auth = auth)
        }

        // Product Detail Screen
        composable(
            route = Screen.ProductDetail.route,
            arguments = listOf(navArgument("productId") { type = NavType.IntType })
        ) { backStackEntry ->
            val productId = backStackEntry.arguments?.getInt("productId")

            ProductDetailScreen(
                navController = navController,
                productId = productId,
                productViewModel = productViewModel,
                cartViewModel = cartViewModel
            )
        }

        // NEW: Payment Screen
        composable(Screen.Payment.route) {
            PaymentScreen(
                navController = navController,
                cartViewModel = cartViewModel
            )
        }
    }
}
