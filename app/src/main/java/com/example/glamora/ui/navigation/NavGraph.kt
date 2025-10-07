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
import com.example.glamora.local.AppDatabase
import com.example.glamora.repository.CartRepository
import com.example.glamora.repository.ProductRepository
import com.example.glamora.ui.screen.*
import com.example.glamora.util.NetworkMonitor
import com.example.glamora.viewmodel.CartViewModel
import com.example.glamora.viewmodel.CartViewModelFactory
import com.example.glamora.viewmodel.LocationViewModel
import com.example.glamora.viewmodel.ProductViewModel
import com.example.glamora.viewmodel.ProductViewModelFactory
import com.example.glamora.viewmodel.ProfileViewModel
import com.google.firebase.auth.FirebaseAuth

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Login : Screen("login")
    object Register : Screen("register")
    object Home : Screen("home")
    object Discover : Screen("discover")
    object Cart : Screen("cart")
    object Profile : Screen("profile")
    object CameraSensor : Screen("camera_sensor")
    object Address : Screen("address")
    object ProductDetail : Screen("product_detail/{productId}") {
        fun createRoute(productId: Int) = "product_detail/$productId"
    }
    object Payment : Screen("payment")
    object OrderHistory : Screen("order_history") // Added Order History route
    object PaymentMethods : Screen("payment_methods") // NEW: Payment Methods screen route
}

@Composable
fun GlamoraNavGraph(
    auth: FirebaseAuth,
    navController: NavHostController = rememberNavController()
) {
    val context = LocalContext.current

    // Network monitor for connectivity checking
    val networkMonitor = remember { NetworkMonitor(context) }

    // Database instance using singleton pattern
    val db = remember {
        AppDatabase.getDatabase(context)
    }

    // Updated CartRepository to include OrderDao and SavedCardDao for complete e-commerce functionality
    val cartRepository = remember {
        CartRepository(db.cartDao(), db.orderDao(), db.savedCardDao()) // Added savedCardDao for payment card management
    }
    val productRepository = remember { ProductRepository(context) }

    // ViewModel factories with updated CartRepository
    val productViewModelFactory = remember { ProductViewModelFactory(productRepository, networkMonitor) }
    val cartViewModelFactory = remember { CartViewModelFactory(cartRepository) }

    // ViewModels - CartViewModel now handles both cart and order operations
    val productViewModel: ProductViewModel = viewModel(factory = productViewModelFactory)
    val cartViewModel: CartViewModel = viewModel(factory = cartViewModelFactory) // Handles cart + orders
    val profileViewModel: ProfileViewModel = viewModel()
    val locationViewModel: LocationViewModel = viewModel()

    NavHost(navController = navController, startDestination = Screen.Splash.route) {

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
            CartScreen(
                navController = navController,
                cartViewModel = cartViewModel // Pass the created ViewModel
            )
        }


        // Profile Screen
        composable(Screen.Profile.route) {
            // FIX: Pass the profileViewModel instance here
            ProfileScreen(
                navController = navController,
                auth = auth,
                profileViewModel = profileViewModel
            )
        }

        composable(Screen.CameraSensor.route) {
            CameraSensorScreen(navController, profileViewModel)
        }

        // NEW: Address Screen
        composable(Screen.Address.route) {
            AddressScreen(
                navController = navController,
                locationViewModel = locationViewModel
            )
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

        // Order History Screen - reuses existing CartViewModel
        composable(Screen.OrderHistory.route) {
            OrderHistoryScreen(
                navController = navController,
                cartViewModel = cartViewModel // Reuses CartViewModel for order data
            )
        }

        // NEW: Payment Methods Screen
        composable(Screen.PaymentMethods.route) {
            PaymentMethodsScreen(
                navController = navController,
                cartViewModel = cartViewModel // Passes CartViewModel to manage saved cards
            )
        }
    }
}
