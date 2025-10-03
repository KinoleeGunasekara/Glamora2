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
import com.example.glamora.viewmodel.ProductUiState
import com.example.glamora.viewmodel.ProductViewModel

// Header title for the screen
private const val DISCOVER_TITLE = "Discover"

@Composable
fun DiscoverScreen(navController: NavController, viewModel: ProductViewModel) {
    val uiState by viewModel.uiState.collectAsState()

    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    // List of product categories including the "All" filter
    val categories = listOf("All", "women's clothing", "men's clothing", "jewelery")
    var selectedCategory by remember { mutableStateOf(categories.first()) }
    var searchQuery by remember { mutableStateOf("") }

    // Defines a vertical color gradient for the entire background, blending surface and background colors for neatness
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
                .background(brush = fullScreenBackgroundBrush) // Applied the gradient to the entire screen
        ) {
            // --------------------------
            // 1. Top Section (Header & Search)
            // --------------------------
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 20.dp) // Spacing before content below
            ) {
                // Renders the main "Discover" title, now smaller and centered
                SimpleHeaderSection(modifier = Modifier.padding(top = 24.dp, bottom = 24.dp))

                // Renders the search bar, now wrapped in a Card for subtle elevation
                SearchBarExact(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier.padding(horizontal = 20.dp)
                )
            }

            // --------------------------
            // 2. Remaining Sections (Banner, Categories, Grid)
            // --------------------------

            Spacer(modifier = Modifier.height(20.dp)) // Added consistent spacing

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
                    Box(
                        Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    }
                }
                is ProductUiState.Error -> {
                    Text(
                        text = (uiState as ProductUiState.Error).message,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(16.dp)
                    )
                }
                is ProductUiState.Success -> {
                    val products = (uiState as ProductUiState.Success).products

                    // Filters products based on selected category and search query
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
 * Renders the main screen title, now smaller and centered horizontally.
 */
@Composable
private fun SimpleHeaderSection(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center // Centers the title horizontally and vertically
    ) {
        Text(
            text = DISCOVER_TITLE,
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 30.sp, // Reduced font size
                letterSpacing = 0.sp,
                color = MaterialTheme.colorScheme.onBackground // Use contrasting color against light background
            )
        )
    }
}

// --------------------------
// Search Bar (Elevated for visibility, slightly darker color)
// --------------------------
/**
 * Renders the search bar wrapped in a Card for subtle elevation and a slightly darker container color.
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
        // Use a small elevation to make the bar slightly visible against the background
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        // Changed containerColor to surfaceVariant for a slightly darker shade
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
            // The TextField itself uses a transparent background/shape because the Card is providing the visual background
            shape = RoundedCornerShape(12.dp),
            colors = TextFieldDefaults.colors(
                // Ensure TextField container colors are transparent or match the card
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                cursorColor = MaterialTheme.colorScheme.primary,
                // Setting indicators to Transparent to remove the underline completely
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                disabledIndicatorColor = Color.Transparent,
                errorIndicatorColor = Color.Transparent,
            ),
            modifier = Modifier.fillMaxSize() // Fills the Card space
        )
    }
}

// --------------------------
// Clean Banner
// --------------------------
/**
 * Displays a clean promotional banner card using a local image resource.
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
            // Note: R.drawable.banner must exist in your project
            Image(
                painter = painterResource(id = R.drawable.banner),
                contentDescription = "Promotional Banner",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
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
 * Renders a horizontally scrollable row of category filter chips with gradient styling for selection.
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
                            category.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() },
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            style = MaterialTheme.typography.titleMedium,
                            color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color.Transparent,
                        containerColor = Color.Transparent,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                        labelColor = MaterialTheme.colorScheme.onSurface
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
 * Displays filtered products in a lazy vertical grid.
 */
@Composable
fun ProductGrid(
    filteredProducts: List<Product>,
    navController: NavController,
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
                onClick = { navController.navigate(Screen.ProductDetail.createRoute(product.id)) },
                onCartClick = { navController.navigate(Screen.Cart.route) }
            )
        }
    }
}