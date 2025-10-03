package com.example.glamora.repository

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import com.example.glamora.api.ApiService
import com.example.glamora.data.Product
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Helper function to check for internet connectivity.
 */
fun isNetworkAvailable(context: Context): Boolean {
    val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val network = connectivityManager.activeNetwork ?: return false
    val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
    return capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)
}

/**
 * Repository handles fetching product data from:
 * 1. API (Retrofit) when online
 * 2. Local JSON file (assets/products.json) when offline
 */
class ProductRepository(private val context: Context) {

    // In-memory cache for quick product lookup
    private val products = mutableListOf<Product>()

    /**
     * Fetch products:
     * - Prefer API if online
     * - If API fails or offline → load from assets (products.json)
     */
    suspend fun getProducts(): List<Product> = withContext(Dispatchers.IO) {
        if (isNetworkAvailable(context)) {
            try {
                val apiProducts = ApiService.productApi.getProducts()
                saveProductsToCache(apiProducts)
                apiProducts
            } catch (e: Exception) {
                // Fallback to local file if API call fails
                loadProductsFromAssets()
            }
        } else {
            // No internet, load offline data
            loadProductsFromAssets()
        }
    }

    /**
     * Load products from local JSON file in assets folder.
     * File name: products.json (make sure it exists in app/src/main/assets/)
     */
    private fun loadProductsFromAssets(): List<Product> {
        return try {
            val json = context.assets.open("products.json") // ✅ Correct filename
                .bufferedReader().use { it.readText() }

            val type = object : TypeToken<List<Product>>() {}.type
            Gson().fromJson<List<Product>>(json, type)
        } catch (e: Exception) {
            emptyList() // Return empty list if parsing fails
        }
    }

    /**
     * Save products to in-memory cache for detail lookup.
     */
    private fun saveProductsToCache(products: List<Product>) {
        this.products.clear()
        this.products.addAll(products)
    }

    /**
     * Get a single product by ID from the in-memory cache.
     */
    fun getProductById(id: Int?): Product? {
        if (id == null) return null
        return products.find { it.id == id }
    }
}
