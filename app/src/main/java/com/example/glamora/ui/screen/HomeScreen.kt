package com.example.glamora.ui.screen

import android.content.res.Configuration
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.glamora.R
import com.example.glamora.data.Product
import com.example.glamora.ui.component.BottomBar
import com.example.glamora.ui.component.ProductCard
import com.example.glamora.ui.navigation.Screen
import com.example.glamora.viewmodel.ProductUiState
import com.example.glamora.viewmodel.ProductViewModel

// Define the maximum number of products to show on the Home Screen as 'few products'
private const val MAX_HOME_PRODUCTS = 6

/**
 * HomeScreen:
 * - Observes ProductViewModel for product list (API or offline JSON).
 * - **Removes category filtering and filter chips** to simplify the home view.
 * - **Displays only the first 6 products** when no search query is active.
 */
@Composable
fun HomeScreen(navController: NavController, viewModel: ProductViewModel) {
    // Collect the product UI state (Loading/Success/Error) from the ViewModel
    val uiState by viewModel.uiState.collectAsState()

    // State for the user's input in the SearchBar
    var searchQuery by remember { mutableStateOf("") }

    // Logic for responsive layout (padding and grid columns) based on screen size/orientation
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    val screenWidthDp = configuration.screenWidthDp

    val horizontalPadding = when {
        screenWidthDp > 840 -> 48.dp
        screenWidthDp > 600 -> 32.dp
        else -> 16.dp
    }

    val gridColumns = when {
        screenWidthDp > 1000 -> 5
        screenWidthDp > 840 -> 4
        screenWidthDp > 600 -> 3
        isLandscape -> 3
        else -> 2
    }

    Scaffold(bottomBar = { BottomBar(navController) }) { padding ->
        when (uiState) {
            is ProductUiState.Loading -> {
                // Show loading indicator
                Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }

            is ProductUiState.Error -> {
                // Show error message
                val message = (uiState as ProductUiState.Error).message
                Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    Text(text = message, color = MaterialTheme.colorScheme.error)
                }
            }

            is ProductUiState.Success -> {
                // Data successfully loaded
                val products = (uiState as ProductUiState.Success).products

                // 1. **Apply Search Filtering**: Filter by title, ignoring category
                val searchFilteredProducts = products.filter { product ->
                    product.title.contains(searchQuery, ignoreCase = true)
                }

                // 2. **Apply Product Limiting**: Show only MAX_HOME_PRODUCTS (6) when not searching.
                // When searching, show all matched products.
                val homeProducts = if (searchQuery.isBlank()) {
                    searchFilteredProducts.take(MAX_HOME_PRODUCTS)
                } else {
                    searchFilteredProducts
                }

                // Choose layout based on orientation
                if (isLandscape) {
                    LandscapeHomeContent(
                        navController = navController,
                        products = homeProducts,
                        searchQuery = searchQuery,
                        onSearchChange = { searchQuery = it },
                        horizontalPadding = horizontalPadding,
                        gridColumns = gridColumns,
                        padding = padding
                    )
                } else {
                    PortraitHomeContent(
                        navController = navController,
                        products = homeProducts,
                        searchQuery = searchQuery,
                        onSearchChange = { searchQuery = it },
                        horizontalPadding = horizontalPadding,
                        gridColumns = gridColumns,
                        padding = padding
                    )
                }
            }
        }
    }
}

// -------------------------------------------------------------------------------------------------

/**
 * Landscape layout structure for the Home Screen.
 * NOTE: Does NOT include category filter chips.
 */
@Composable
fun LandscapeHomeContent(
    navController: NavController,
    products: List<Product>, // The list contains limited/searched products
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    horizontalPadding: Dp,
    gridColumns: Int,
    padding: PaddingValues
) {
    Row(
        modifier = Modifier
            .padding(padding)
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Left side: Banner image
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

        // Right side: Search and Product Grid
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .weight(1f)
                .padding(horizontal = horizontalPadding)
        ) {
            val scrollState = rememberScrollState()
            Column(modifier = Modifier.verticalScroll(scrollState)) {
                Spacer(modifier = Modifier.height(16.dp))
                // Search bar
                SearchBar(searchQuery, onSearchChange)
                // Spacer replacing the removed Category Chips
                Spacer(modifier = Modifier.height(20.dp))
                // Header row: "Popular Products" (showing the limited set) and "See All" link
                HeaderRow(navController)
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Product grid (showing max 6 products if not searching)
            LazyVerticalGrid(
                columns = GridCells.Fixed(gridColumns),
                contentPadding = PaddingValues(top = 8.dp, bottom = 80.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth().weight(1f)
            ) {
                items(products) { product ->
                    ProductCard(
                        product = product,
                        onClick = { navController.navigate(Screen.ProductDetail.createRoute(product.id)) },
                        onCartClick = { navController.navigate(Screen.Cart.route) }
                    )
                }
            }
        }
    }
}

// -------------------------------------------------------------------------------------------------

/**
 * Portrait layout structure for the Home Screen.
 * NOTE: Does NOT include category filter chips.
 */
@Composable
fun PortraitHomeContent(
    navController: NavController,
    products: List<Product>, // The list contains limited/searched products
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    horizontalPadding: Dp,
    gridColumns: Int,
    padding: PaddingValues
) {
    Column(
        modifier = Modifier
            .padding(padding)
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        val scrollState = rememberScrollState()
        Column(
            modifier = Modifier
                .verticalScroll(scrollState)
                .padding(horizontal = horizontalPadding)
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            // Search bar
            SearchBar(searchQuery, onSearchChange)
            // Spacer replacing the removed Category Chips
            Spacer(modifier = Modifier.height(20.dp))
            // Banner image
            Image(
                painter = painterResource(id = R.drawable.banner),
                contentDescription = stringResource(R.string.promotional_banner),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .clip(RoundedCornerShape(20.dp)),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.height(24.dp))
            // Header row: "Popular Products" (showing the limited set) and "See All" link
            HeaderRow(navController)
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Product grid (showing max 6 products if not searching)
        LazyVerticalGrid(
            columns = GridCells.Fixed(gridColumns),
            contentPadding = PaddingValues(
                start = horizontalPadding,
                end = horizontalPadding,
                top = 8.dp,
                bottom = 80.dp
            ),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth().weight(1f)
        ) {
            items(products) { product ->
                ProductCard(
                    product = product,
                    onClick = { navController.navigate(Screen.ProductDetail.createRoute(product.id)) },
                    onCartClick = { navController.navigate(Screen.Cart.route) }
                )
            }
        }
    }
}

// -------------------------------------------------------------------------------------------------

/**
 * SearchBar Composable: Allows users to search for products by title.
 */
@Composable
fun SearchBar(query: String, onQueryChange: (String) -> Unit) {
    Surface(
        shape = RoundedCornerShape(50),
        tonalElevation = 4.dp,
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(8.dp))
            BasicTextField(
                value = query,
                onValueChange = onQueryChange,
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                textStyle = LocalTextStyle.current.copy(
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 16.sp
                ),
                decorationBox = { innerTextField ->
                    Box {
                        if (query.isEmpty()) {
                            Text(
                                text = "Search products...",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 16.sp
                            )
                        }
                        innerTextField()
                    }
                }
            )
        }
    }
}

// -------------------------------------------------------------------------------------------------

/**
 * HeaderRow Composable: Displays "Popular Products" and a clickable "See All" link.
 */
@Composable
fun HeaderRow(navController: NavController) {
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = "Popular Products",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = "See All",
            color = MaterialTheme.colorScheme.primary,
            fontSize = 14.sp,
            modifier = Modifier.clickable { navController.navigate(Screen.Discover.route) }
        )
    }
}