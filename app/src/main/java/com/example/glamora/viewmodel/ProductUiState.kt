package com.example.glamora.viewmodel

import com.example.glamora.data.Product

/**
 * Represents different states the Product UI can be in:
 * - Loading: when data is being fetched.
 * - Success: when products are successfully fetched.
 * - Error: when something goes wrong.
 */
sealed class ProductUiState {
    object Loading : ProductUiState()

    // Success contains a list of products from API or local JSON
    data class Success(val products: List<Product>) : ProductUiState()

    // Error contains a message to display in UI
    data class Error(val message: String) : ProductUiState()
}
