package com.example.glamora.ui.screen

import android.content.res.Configuration
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.glamora.R
import com.example.glamora.data.Product
import com.example.glamora.ui.component.BottomBar
import com.example.glamora.ui.component.ProductCard
import com.example.glamora.ui.navigation.Screen
import com.example.glamora.viewmodel.CartViewModel
import com.example.glamora.viewmodel.ProductUiState
import com.example.glamora.viewmodel.ProductViewModel

// Maximum number of featured products to display on the home screen by default
private const val MAX_HOME_PRODUCTS = 6

/**
 * ---------------------------- HOME SCREEN MAIN ENTRY POINT ----------------------------
 *
 * This composable represents the main Home screen of the Glamora app.
 * Responsibilities:
 * - Observes the ProductViewModel to display product data.
 * - Displays static top section (Search + Banner).
 * - Renders a responsive, scrollable product grid layout.
 * - Supports both portrait and landscape orientations.
 */
@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: ProductViewModel,
    cartViewModel: CartViewModel
) {
    // Collect the current UI state (Loading, Error, Success) from the ViewModel.
    val uiState by viewModel.uiState.collectAsState()

    // State variable to store user search input.
    var searchQuery by remember { mutableStateOf("") }

    // Detect current screen configuration (used for responsive design).
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    val screenWidthDp = configuration.screenWidthDp

    // Dynamically adjust horizontal padding based on screen width.
    val horizontalPadding = when {
        screenWidthDp > 840 -> 48.dp
        screenWidthDp > 600 -> 32.dp
        else -> 16.dp
    }

    // Dynamically adjust grid columns based on available width or orientation.
    val gridColumns = when {
        screenWidthDp > 1000 -> 5
        screenWidthDp > 840 -> 4
        screenWidthDp > 600 -> 3
        isLandscape -> 3
        else -> 2
    }

    // Scaffold provides a standard app layout structure with BottomBar.
    Scaffold(bottomBar = { BottomBar(navController) }) { padding ->
        when (uiState) {

            // ----------------------------- LOADING STATE -----------------------------
            is ProductUiState.Loading -> {
                // Display a loading spinner centered on screen.
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            // ----------------------------- ERROR STATE -----------------------------
            is ProductUiState.Error -> {
                // Display an error message if data fetch fails.
                val message = (uiState as ProductUiState.Error).message
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = message, color = MaterialTheme.colorScheme.error)
                }
            }

            // ----------------------------- SUCCESS STATE -----------------------------
            is ProductUiState.Success -> {
                // Extract list of products from ViewModel.
                val products = (uiState as ProductUiState.Success).products

                // Filter products by search query (case-insensitive).
                val searchFilteredProducts = products.filter {
                    it.title.contains(searchQuery, ignoreCase = true)
                }

                // If search query is blank, show only first 6 items.
                // Otherwise, show all matching results.
                val homeProducts = if (searchQuery.isBlank()) {
                    searchFilteredProducts.take(MAX_HOME_PRODUCTS)
                } else searchFilteredProducts

                // Use separate composables for portrait and landscape layout handling.
                if (isLandscape) {
                    LandscapeHomeContent(
                        navController = navController,
                        products = homeProducts,
                        searchQuery = searchQuery,
                        onSearchChange = { searchQuery = it },
                        horizontalPadding = horizontalPadding,
                        gridColumns = gridColumns,
                        padding = padding,
                        cartViewModel = cartViewModel
                    )
                } else {
                    PortraitHomeContent(
                        navController = navController,
                        products = homeProducts,
                        searchQuery = searchQuery,
                        onSearchChange = { searchQuery = it },
                        horizontalPadding = horizontalPadding,
                        gridColumns = gridColumns,
                        padding = padding,
                        cartViewModel = cartViewModel
                    )
                }
            }
        }
    }
}

/* ============================================================================================= */
/* =============================== PORTRAIT HOME LAYOUT ======================================== */
/* ============================================================================================= */

/**
 * Portrait layout of the Home screen.
 * The upper section (Search bar + Banner) is static,
 * and only the product grid is scrollable.
 */
@Composable
fun PortraitHomeContent(
    navController: NavController,
    products: List<Product>,
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    horizontalPadding: Dp,
    gridColumns: Int,
    padding: PaddingValues,
    cartViewModel: CartViewModel
) {
    Column(
        modifier = Modifier
            .padding(padding)
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = horizontalPadding)
    ) {
        // --- STATIC HEADER SECTION ---
        Spacer(modifier = Modifier.height(16.dp))

        // Top search bar component
        SearchBar(searchQuery, onSearchChange)

        Spacer(modifier = Modifier.height(20.dp))

        // Promotional banner image
        Image(
            painter = painterResource(id = R.drawable.banner),
            contentDescription = stringResource(R.string.promotional_banner),
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .clip(RoundedCornerShape(20.dp)),
            contentScale = ContentScale.Crop
        )

        Spacer(modifier = Modifier.height(20.dp))

        // --- SCROLLABLE PRODUCT GRID SECTION ---
        // Box with weight(1f) allows this area to expand and scroll,
        // while keeping the header fixed.
        Box(modifier = Modifier.weight(1f)) {
            HomeProductGrid(
                products = products,
                navController = navController,
                gridColumns = gridColumns,
                cartViewModel = cartViewModel
            )
        }
    }
}

/* ============================================================================================= */
/* =============================== LANDSCAPE HOME LAYOUT ======================================= */
/* ============================================================================================= */

/**
 * Landscape layout for wider screens (tablets, horizontal phones).
 * Banner is positioned on the left and content on the right.
 */
@Composable
fun LandscapeHomeContent(
    navController: NavController,
    products: List<Product>,
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    horizontalPadding: Dp,
    gridColumns: Int,
    padding: PaddingValues,
    cartViewModel: CartViewModel
) {
    Row(
        modifier = Modifier
            .padding(padding)
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // --- STATIC LEFT-SIDE BANNER ---
        Image(
            painter = painterResource(id = R.drawable.banner),
            contentDescription = stringResource(R.string.promotional_banner),
            modifier = Modifier
                .fillMaxHeight()
                .widthIn(min = 200.dp, max = 300.dp)
                .clip(RoundedCornerShape(20.dp))
                .padding(16.dp),
            contentScale = ContentScale.Crop
        )

        // --- RIGHT-SIDE CONTENT (Search + Product Grid) ---
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .weight(1f)
                .padding(horizontal = horizontalPadding)
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            SearchBar(searchQuery, onSearchChange)
            Spacer(modifier = Modifier.height(16.dp))

            // Scrollable grid area
            Box(modifier = Modifier.weight(1f)) {
                HomeProductGrid(
                    products = products,
                    navController = navController,
                    gridColumns = gridColumns,
                    cartViewModel = cartViewModel
                )
            }
        }
    }
}

/* ============================================================================================= */
/* =============================== PRODUCT GRID COMPONENT ====================================== */
/* ============================================================================================= */

/**
 * Displays a scrollable grid of product cards.
 * Each product card supports navigation to detail view and "add to cart" action.
 */
@Composable
fun HomeProductGrid(
    products: List<Product>,
    navController: NavController,
    gridColumns: Int,
    cartViewModel: CartViewModel
) {
    Column {
        // Section title
        Text(
            text = "Featured Products",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // LazyVerticalGrid efficiently renders large lists
        // and only composes visible items for performance.
        LazyVerticalGrid(
            columns = GridCells.Fixed(gridColumns),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 16.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(products) { product ->
                ProductCard(
                    product = product,
                    // Clicking the card navigates to Product Detail screen
                    onClick = { navController.navigate(Screen.ProductDetail.createRoute(product.id)) },
                    // Clicking the cart icon adds the product and navigates to cart
                    onCartClick = {
                        cartViewModel.addItem(product)
                        navController.navigate(Screen.Cart.route)
                    }
                )
            }
        }
    }
}

/* ============================================================================================= */
/* =============================== SEARCH BAR COMPONENT ======================================== */
/* ============================================================================================= */

/**
 * Simple custom search bar used in the Home screen.
 * - Accepts input text from user.
 * - Displays placeholder when empty.
 * - Emits text changes via onValueChange callback.
 */
@Composable
fun SearchBar(value: String, onValueChange: (String) -> Unit) {
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        singleLine = true,
        decorationBox = { innerTextField ->
            Row(
                Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Search icon on left
                Icon(
                    Icons.Default.Search,
                    contentDescription = "Search",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    modifier = Modifier.padding(end = 8.dp)
                )

                // Input area + placeholder text
                Box(Modifier.weight(1f)) {
                    if (value.isEmpty()) {
                        Text(
                            "Search for products...",
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                    }
                    innerTextField()
                }
            }
        }
    )
}
