package com.example.glamora.ui.screen

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.glamora.ui.component.ProductImageLoader
import com.example.glamora.ui.navigation.Screen // Import Screen to access Cart route
import com.example.glamora.viewmodel.CartUiEvent
import com.example.glamora.viewmodel.CartViewModel
import com.example.glamora.viewmodel.ProductUiState
import com.example.glamora.viewmodel.ProductViewModel

// --- Custom Composable for Image-Matching Size Selection (Bordered/Highlighted) ---
@Composable
fun ImageStyleSizeChip(
    size: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    primaryColor: Color
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .defaultMinSize(minWidth = 50.dp, minHeight = 40.dp)
            .clip(RoundedCornerShape(8.dp))
            .border(
                width = 1.dp,
                color = if (isSelected) primaryColor else Color.LightGray.copy(alpha = 0.5f),
                shape = RoundedCornerShape(8.dp)
            )
            .background(
                color = if (isSelected) primaryColor.copy(alpha = 0.1f) else Color.Transparent
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(
            text = size,
            fontWeight = FontWeight.Medium,
            color = if (isSelected) primaryColor else MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductDetailScreen(
    navController: NavController,
    productId: Int?,
    productViewModel: ProductViewModel,
    cartViewModel: CartViewModel
) {
    // State to control the display of the Snackbar message
    val snackbarHostState = remember { SnackbarHostState() }

    // Load product data when the screen is first composed or productId changes
    LaunchedEffect(productId) {
        if (productId != null) {
            productViewModel.loadProductById(productId)
        }
    }

    // FIX: LaunchedEffect to observe CartViewModel events and handle navigation
    LaunchedEffect(Unit) {
        // Collects one-time events from the ViewModel
        cartViewModel.eventFlow.collect { event ->
            when (event) {
                is CartUiEvent.ItemAdded -> {
                    // Show Snackbar and store the result of user interaction
                    val result = snackbarHostState.showSnackbar(
                        message = "${event.productName} added to cart!",
                        actionLabel = "View Cart",
                        duration = SnackbarDuration.Short
                    )

                    // Check if the user clicked the action label
                    if (result == SnackbarResult.ActionPerformed) {
                        // Navigate to the Cart screen
                        navController.navigate(Screen.Cart.route)
                    }
                }
            }
        }
    }

    val product by productViewModel.selectedProduct.collectAsState()
    val uiState by productViewModel.uiState.collectAsState()

    // State for UI selections (quantity, size, color)
    var quantity by remember { mutableStateOf(1) }
    var selectedSize by remember { mutableStateOf("M") }
    var selectedColor by remember { mutableStateOf(Color.Red) }
    var readMore by remember { mutableStateOf(false) }

    val screenHorizontalPadding = 20.dp
    val primaryPink = Color(0xFFE91E63)

    Scaffold(
        // Inject the SnackbarHost into the Scaffold
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { /* Handle Favorite */ }) {
                        Icon(Icons.Default.FavoriteBorder, contentDescription = "Favorite")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        },
        bottomBar = {
            if (product != null) {
                BottomAppBar(
                    containerColor = MaterialTheme.colorScheme.surface
                ) {
                    Button(
                        // ACTION: Calls the ViewModel to add item (which emits the success event)
                        onClick = { product?.let { cartViewModel.addItem(it) } },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .padding(horizontal = screenHorizontalPadding, vertical = 8.dp),
                        shape = RoundedCornerShape(24.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = primaryPink)
                    ) {
                        Text("Add to Cart", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    }
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        when (uiState) {
            is ProductUiState.Loading -> {
                Box(
                    Modifier.fillMaxSize().padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) { CircularProgressIndicator() }
            }
            is ProductUiState.Error -> {
                Box(
                    Modifier.fillMaxSize().padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) { Text("Error loading product") }
            }
            is ProductUiState.Success -> {
                val currentProduct = product
                if (currentProduct == null) {
                    Box(
                        Modifier.fillMaxSize().padding(paddingValues),
                        contentAlignment = Alignment.Center
                    ) { Text("Product data is unavailable.") }
                    return@Scaffold
                }

                Column(
                    Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(paddingValues)
                ) {
                    // --- 1. Product Image ---
                    ProductImageLoader(
                        imageUrl = currentProduct.image,
                        contentDescription = currentProduct.title,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(300.dp)
                            .clip(RoundedCornerShape(32.dp))
                            .background(Color(0xFFFFEBEE))
                    )

                    // --- Content Section ---
                    Column(
                        Modifier
                            .padding(horizontal = screenHorizontalPadding)
                            .padding(top = 16.dp)
                    ) {

                        // --- 2. Title and Rating ---
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Text(
                                currentProduct.title,
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    lineHeight = 30.sp,
                                    fontSize = 25.sp
                                ),
                                modifier = Modifier.weight(1f)
                            )

                            // Rating Chip
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .padding(start = 12.dp, top = 4.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(primaryPink.copy(alpha = 0.15f))
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Icon(Icons.Default.Star, contentDescription = null, tint = primaryPink, modifier = Modifier.size(16.dp))
                                Text("4.8", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(start = 4.dp))
                            }
                        }

                        Spacer(Modifier.height(10.dp))

                        // --- 3. Category Tag ---
                        Text(
                            text = currentProduct.category.uppercase(),
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(primaryPink.copy(alpha = 0.1f))
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                            color = primaryPink
                        )

                        Spacer(Modifier.height(24.dp))

                        // --- 4. Price, Discount, & Quantity Control ---
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.Bottom) {
                                Text(
                                    "$%.2f".format(currentProduct.price),
                                    style = MaterialTheme.typography.headlineMedium,
                                    color = primaryPink,
                                    fontWeight = FontWeight.Bold
                                )
                                // Placeholder for original price / discount price
                                Text(
                                    "$99.00",
                                    style = MaterialTheme.typography.titleMedium.copy(textDecoration = TextDecoration.LineThrough),
                                    color = Color.Gray,
                                    modifier = Modifier.padding(start = 8.dp, bottom = 2.dp)
                                )
                            }


                            // Quantity Control
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(onClick = { if (quantity > 1) quantity-- }) {
                                    Icon(Icons.Default.Remove, contentDescription = "Decrease", tint = Color.Gray)
                                }
                                Text(
                                    "$quantity",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.padding(horizontal = 8.dp)
                                )
                                IconButton(onClick = { quantity++ }) {
                                    Icon(Icons.Default.Add, contentDescription = "Increase", tint = Color.Gray)
                                }
                            }
                        }

                        Spacer(Modifier.height(16.dp))

                        // --- 5. Description (Read More/Show Less) ---
                        val descriptionLimit = 150
                        val fullDescription = currentProduct.description
                        val truncatedText = fullDescription.take(descriptionLimit).trim() + if (fullDescription.length > descriptionLimit && !readMore) "..." else ""
                        val descriptionText = if (readMore) fullDescription else truncatedText

                        Text(
                            text = descriptionText,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        // Read More/Show Less Link
                        if (fullDescription.length > descriptionLimit) {
                            Text(
                                text = if (readMore) "Show Less" else "Read More",
                                color = primaryPink,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier
                                    .clickable { readMore = !readMore }
                                    .padding(vertical = 4.dp)
                            )
                        }

                        Spacer(Modifier.height(16.dp))

                        // --- 6. Special Offer Tag ---
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(vertical = 8.dp)
                        ) {
                            Icon(
                                Icons.Default.Discount,
                                contentDescription = "Special Offer",
                                tint = primaryPink,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                "Special 50% off limited time offer!",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = primaryPink,
                                modifier = Modifier.padding(start = 8.dp)
                            )
                        }

                        Spacer(Modifier.height(24.dp))

                        // --- 7. Sizes (Custom chip with border highlight) ---
                        Text(
                            text = "Select Size",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(Modifier.height(12.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            listOf("S", "M", "L", "XL").forEach { size ->
                                ImageStyleSizeChip(
                                    size = size,
                                    isSelected = selectedSize == size,
                                    onClick = { selectedSize = size },
                                    primaryColor = primaryPink
                                )
                            }
                        }

                        Spacer(Modifier.height(24.dp))

                        // --- 8. Colors (Highlight without grey border when unselected) ---
                        Text(
                            text = "Select Color",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(Modifier.height(12.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            listOf(Color.Gray, Color.Black, Color.Red, Color.Blue).forEach { color ->
                                Box(
                                    Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(color)
                                        .border(
                                            width = if (selectedColor == color) 3.dp else 0.dp,
                                            color = primaryPink,
                                            shape = CircleShape
                                        )
                                        .clickable { selectedColor = color }
                                )
                            }
                        }

                        Spacer(Modifier.height(48.dp))
                    }
                }
            }
        }
    }
}