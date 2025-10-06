package com.example.glamora.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.glamora.data.Product
import com.example.glamora.repository.ProductRepository
import com.example.glamora.util.ConnectionState
import com.example.glamora.util.NetworkMonitor
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * ViewModel that manages product data and reacts to network changes.
 * - Fetches from API / External / Local JSON automatically.
 * - Exposes current connection state and product list to the UI.
 */
class ProductViewModel(
    private val repository: ProductRepository,
    networkMonitor: NetworkMonitor
) : ViewModel() {

    private val _uiState = MutableStateFlow<ProductUiState>(ProductUiState.Loading)
    val uiState: StateFlow<ProductUiState> = _uiState.asStateFlow()

    private val _selectedProduct = MutableStateFlow<Product?>(null)
    val selectedProduct: StateFlow<Product?> = _selectedProduct.asStateFlow()

    // Observe network changes as StateFlow
    val networkState: StateFlow<ConnectionState> = networkMonitor.isConnected
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = ConnectionState.Unavailable
        )

    init {
        Log.d("ProductViewModel", "Initialized, observing network changes...")
        observeNetworkAndLoad()
    }

    /**
     * Watches network state and triggers data fetching accordingly.
     */
    private fun observeNetworkAndLoad() {
        viewModelScope.launch {
            networkState.collectLatest { state ->
                when (state) {
                    is ConnectionState.Available -> loadProducts(forceOnline = true)
                    is ConnectionState.Unavailable -> loadProducts(forceOnline = false)
                }
            }
        }
    }

    /**
     * Loads products asynchronously.
     * @param forceOnline true → use API/external; false → local JSON only.
     */
    fun loadProducts(forceOnline: Boolean = true) {
        viewModelScope.launch {
            _uiState.value = ProductUiState.Loading
            try {
                val products = if (forceOnline) {
                    repository.getProducts()
                } else {
                    repository.loadProductsFromAssets()
                }
                _uiState.value = ProductUiState.Success(products)
            } catch (e: Exception) {
                Log.e("ProductViewModel", "Error loading products", e)
                _uiState.value = ProductUiState.Error("Failed to load products: ${e.message}")
            }
        }
    }

    /**
     * Returns product by ID (from in-memory cache).
     */
    fun loadProductById(productId: Int) {
        _selectedProduct.value = repository.getProductById(productId)
    }
}
