package com.example.glamora.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.glamora.data.Product
import com.example.glamora.repository.ProductRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for managing product-related data.
 * Uses ProductRepository for fetching products (online/offline).
 */
class ProductViewModel(private val repository: ProductRepository) : ViewModel() {

    // Backing property for UI state (Loading / Success / Error)
    private val _uiState = MutableStateFlow<ProductUiState>(ProductUiState.Loading)
    val uiState: StateFlow<ProductUiState> = _uiState

    // Holds a single product for the ProductDetailScreen
    private val _selectedProduct = MutableStateFlow<Product?>(null)
    val selectedProduct: StateFlow<Product?> = _selectedProduct

    init {
        Log.d("ProductViewModel", "ViewModel initialized, loading products...")
        loadProducts()
    }

    /**
     * Loads products asynchronously from the repository.
     * - Shows Loading first
     * - If success → updates UI with product list
     * - If error → shows error message
     */
    fun loadProducts() {
        viewModelScope.launch {
            _uiState.value = ProductUiState.Loading
            try {
                val products = repository.getProducts()
                _uiState.value = ProductUiState.Success(products)
            } catch (e: Exception) {
                _uiState.value = ProductUiState.Error("Failed to load products: ${e.message}")
            }
        }
    }

    /**
     * Loads a single product by its ID from repository cache.
     */
    fun loadProductById(productId: Int) {
        _selectedProduct.value = repository.getProductById(productId)
    }
}
