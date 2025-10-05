package com.example.glamora.repository

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import com.example.glamora.api.ApiService
import com.example.glamora.data.Product
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Helper function to check if the device has an active internet connection.
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
 * Repository to fetch product data from multiple sources:
 * 1. Public API (primary)
 * 2. External JSON (fallback)
 * 3. Local JSON (offline backup)
 */
class ProductRepository(private val context: Context) {

    // In-memory cache used for fast lookup (e.g., when viewing details)
    private val products = mutableListOf<Product>()

    /**
     * Loads products using the best available source.
     * Always saves successfully loaded data into the cache for product detail lookup.
     */
    suspend fun getProducts(): List<Product> = withContext(Dispatchers.IO) {
        if (isNetworkAvailable(context)) {
            try {
                // ---  Try Public API first ---
                val apiProducts = ApiService.productApi.getProducts()
                saveProductsToCache(apiProducts)
                Log.d("ProductRepository", "Loaded data from Public API")
                return@withContext apiProducts
            } catch (apiError: Exception) {
                Log.e("ProductRepository", "API failed, trying External JSON", apiError)
                try {
                    // ---  Fallback: External JSON (GitHub-hosted) ---
                    val externalProducts = ApiService.productApi.getExternalJson()
                    saveProductsToCache(externalProducts)
                    Log.d("ProductRepository", "Loaded data from External JSON")
                    return@withContext externalProducts
                } catch (jsonError: Exception) {
                    // ---  Final Fallback: Local JSON ---
                    Log.e("ProductRepository", "External JSON failed, using Local JSON", jsonError)
                    val localProducts = loadProductsFromAssets()
                    saveProductsToCache(localProducts) // FIX: Cache local data
                    return@withContext localProducts
                }
            }
        } else {
            // --- OFFLINE MODE ---
            Log.w("ProductRepository", "Offline mode: loading from Local JSON")
            val localProducts = loadProductsFromAssets()
            saveProductsToCache(localProducts) //  FIX: Cache local data
            return@withContext localProducts
        }
    }

    /**
     * Reads product data from assets/products.json.
     * This file should exist under app/src/main/assets/.
     */
    private fun loadProductsFromAssets(): List<Product> {
        return try {
            val json = context.assets.open("products.json").bufferedReader().use { it.readText() }
            val type = object : TypeToken<List<Product>>() {}.type
            val localProducts = Gson().fromJson<List<Product>>(json, type)
            Log.d("ProductRepository", "Loaded data from Local JSON: ${localProducts.size} items")
            localProducts
        } catch (e: Exception) {
            Log.e("ProductRepository", "Failed to load Local JSON", e)
            emptyList()
        }
    }

    /**
     * Caches products in memory so ProductDetailScreen can find them quickly by ID.
     */
    private fun saveProductsToCache(products: List<Product>) {
        this.products.clear()
        this.products.addAll(products)
    }

    /**
     * Returns a product from the in-memory cache by its ID.
     */
    fun getProductById(id: Int?): Product? {
        if (id == null) return null
        val found = products.find { it.id == id }
        if (found == null) {
            Log.w("ProductRepository", "Product with ID=$id not found in cache.")
        }
        return found
    }
}
