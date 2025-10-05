package com.example.glamora.ui.screen

import android.content.res.Configuration
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.glamora.R
import com.example.glamora.data.Product
import com.example.glamora.ui.component.BottomBar
import com.example.glamora.ui.component.ProductCard
import com.example.glamora.ui.navigation.Screen
import com.example.glamora.viewmodel.CartViewModel // REQUIRED IMPORT
import com.example.glamora.viewmodel.ProductUiState
import com.example.glamora.viewmodel.ProductViewModel

// Header title for the screen
private const val DISCOVER_TITLE = "Discover"

@Composable
fun DiscoverScreen(
    navController: NavController,
    viewModel: ProductViewModel,
    cartViewModel: CartViewModel // Must be passed from NavGraph
) {
    val uiState by viewModel.uiState.collectAsState()

    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    // List of product categories including the "All" filter
    val categories = listOf("All", "women's clothing", "men's clothing", "jewelery")
    var selectedCategory by remember { mutableStateOf(categories.first()) }
    var searchQuery by remember { mutableStateOf("") }

    // Defines a vertical color gradient for the entire background.
    val fullScreenBackgroundBrush = Brush.verticalGradient(
        colors = listOf(
            MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
            MaterialTheme.colorScheme.background,
        )
    )

    Scaffold(
        // Displays the navigation bar at the bottom of the screen
        bottomBar = { BottomBar(navController) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(brush = fullScreenBackgroundBrush) // Applies the background gradient
        ) {
            // --------------------------
            // 1. Top Section (Header & Search)
            // --------------------------
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 20.dp)
            ) {
                // Renders the main "Discover" title
                SimpleHeaderSection(modifier = Modifier.padding(top = 24.dp, bottom = 24.dp))

                // Renders the elevated search bar
                SearchBarExact(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier.padding(horizontal = 20.dp)
                )
            }

            // --------------------------
            // 2. Remaining Sections (Banner, Categories, Grid)
            // --------------------------

            Spacer(modifier = Modifier.height(20.dp))

            // Clean Banner, visible only in portrait mode
            if (!isLandscape) {
                CleanBanner(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .padding(horizontal = 16.dp)
                )
                Spacer(modifier = Modifier.height(20.dp))
            }

            // Renders the horizontally scrollable category chips
            EnhancedCategoryChips(categories, selectedCategory) { selectedCategory = it }

            Spacer(modifier = Modifier.height(16.dp))

            // Product Grid / Dynamic content loading and filtering logic
            when (uiState) {
                is ProductUiState.Loading -> {
                    // Displays a circular indicator while products are loading
                    Box(
                        Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    }
                }
                is ProductUiState.Error -> {
                    // Displays an error message if product loading fails
                    Text(
                        text = (uiState as ProductUiState.Error).message,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(16.dp)
                    )
                }
                is ProductUiState.Success -> {
                    val products = (uiState as ProductUiState.Success).products

                    // Filters the product list based on the selected category and the search query
                    val filteredProducts = products.filter { product ->
                        val categoryMatch = selectedCategory == "All" ||
                                product.category.equals(selectedCategory, ignoreCase = true)
                        val searchMatch = product.title.contains(searchQuery, ignoreCase = true)
                        categoryMatch && searchMatch
                    }

                    if (isLandscape) {
                        // Arranges Banner and Grid side-by-side in landscape mode
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp)
                                .padding(bottom = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            CleanBanner(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                            )
                            ProductGrid(
                                filteredProducts,
                                navController,
                                cartViewModel, // Passing ViewModel to the grid
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                            )
                        }
                    } else {
                        // Displays the product grid vertically in portrait mode
                        ProductGrid(
                            filteredProducts,
                            navController,
                            cartViewModel, // Passing ViewModel to the grid
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp)
                        )
                    }
                }
            }
        }
    }
}

// --------------------------
// Simple Header Section
// --------------------------
/**
 * Renders the main screen title, centered horizontally.
 */
@Composable
private fun SimpleHeaderSection(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = DISCOVER_TITLE,
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 30.sp,
                letterSpacing = 0.sp,
                color = MaterialTheme.colorScheme.onBackground
            )
        )
    }
}

// --------------------------
// Search Bar (Elevated for visibility)
// --------------------------
/**
 * Renders the elevated search bar using a Card wrapper for visual separation.
 */
@Composable
private fun SearchBarExact(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp),
        shape = RoundedCornerShape(12.dp),
        // Provides subtle elevation for the search bar
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        TextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = {
                Text(
                    "Search anything, explore everything",
                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = 16.sp),
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
            },
            trailingIcon = {
                Icon(
                    Icons.Default.Search,
                    contentDescription = "Search icon",
                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
            },
            // Makes the internal TextField background transparent to show the Card's background
            shape = RoundedCornerShape(12.dp),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                cursorColor = MaterialTheme.colorScheme.primary,
                // Removes the default underline indicator
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
            ),
            modifier = Modifier.fillMaxSize()
        )
    }
}

// --------------------------
// Clean Banner
// --------------------------
/**
 * Displays a promotional banner card using a local image resource.
 */
@Composable
private fun CleanBanner(modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Renders the banner image, assuming R.drawable.banner exists
            Image(
                painter = painterResource(id = R.drawable.banner),
                contentDescription = "Promotional Banner",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            // Subtle overlay for visual effect
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.05f))
            )
        }
    }
}

// --------------------------
// Enhanced Category Chips
// --------------------------
/**
 * Renders a horizontally scrollable row of category filter chips.
 */
@Composable
fun EnhancedCategoryChips(
    categories: List<String>,
    selectedCategory: String,
    onSelect: (String) -> Unit
) {
    val gradientColors = listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.secondary)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        categories.forEach { category ->
            val isSelected = selectedCategory == category

            // Outer box creates the background for the selected state gradient
            Box(
                modifier = Modifier
                    .background(
                        color = if (isSelected) Color.Transparent else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                        shape = RoundedCornerShape(24.dp)
                    )
                    .then(if (isSelected) Modifier.background(
                        brush = Brush.horizontalGradient(gradientColors),
                        shape = RoundedCornerShape(24.dp)
                    ) else Modifier)
            ) {
                FilterChip(
                    selected = isSelected,
                    onClick = { onSelect(category) },
                    label = {
                        Text(
                            // Capitalizes the first letter for display
                            category.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() },
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            style = MaterialTheme.typography.titleMedium,
                            color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                        )
                    },
                    // Makes the chip body transparent since the outer Box provides the background/gradient
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color.Transparent,
                        containerColor = Color.Transparent,
                    ),
                    border = null,
                    modifier = Modifier.heightIn(min = 40.dp)
                )
            }
        }
    }
}

// --------------------------
// Product Grid
// --------------------------
/**
 * Displays filtered products in a lazy vertical grid and handles the product card actions.
 */
@Composable
fun ProductGrid(
    filteredProducts: List<Product>,
    navController: NavController,
    cartViewModel: CartViewModel, // Accepts the ViewModel to perform cart actions
    modifier: Modifier = Modifier
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
        contentPadding = PaddingValues(bottom = 20.dp),
        modifier = modifier.fillMaxSize()
    ) {
        items(filteredProducts) { product ->
            ProductCard(
                product = product,
                // Card click navigates to the detailed screen
                onClick = { navController.navigate(Screen.ProductDetail.createRoute(product.id)) },
                // Cart icon click performs two actions:
                onCartClick = {
                    // 1. Adds the product to the cart
                    cartViewModel.addItem(product)
                    // 2. Navigates immediately to the Cart screen
                    navController.navigate(Screen.Cart.route)
                }
            )
        }
    }
}