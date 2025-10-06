package com.example.glamora.repository

import android.content.Context
import android.util.Log
import com.example.glamora.api.ApiService
import com.example.glamora.data.Product
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Repository to fetch product data from multiple sources:
 * 1. Public API (primary)
 * 2. External JSON (fallback)
 * 3. Local JSON (offline backup in assets/products.json)
 */
class ProductRepository(private val context: Context) {

    // In-memory cache for fast access
    private val products = mutableListOf<Product>()

    /**
     * Loads products using the best available source.
     * Tries: API → External JSON → Local JSON.
     */
    suspend fun getProducts(): List<Product> = withContext(Dispatchers.IO) {
        try {
            val apiProducts = ApiService.productApi.getProducts()
            saveProductsToCache(apiProducts)
            Log.d("ProductRepository", "Loaded data from Public API")
            apiProducts
        } catch (apiError: Exception) {
            Log.e("ProductRepository", "API failed, trying External JSON", apiError)
            try {
                val externalProducts = ApiService.productApi.getExternalJson()
                saveProductsToCache(externalProducts)
                Log.d("ProductRepository", "Loaded data from External JSON")
                externalProducts
            } catch (jsonError: Exception) {
                Log.e("ProductRepository", "External JSON failed, loading Local JSON", jsonError)
                loadProductsFromAssets(cacheAndLog = true)
            }
        }
    }

    /**
     * Reads products from assets/products.json (offline mode).
     */
    suspend fun loadProductsFromAssets(): List<Product> = withContext(Dispatchers.IO) {
        loadProductsFromAssets(cacheAndLog = true)
    }

    /**
     * Internal helper to read local JSON and optionally cache it.
     */
    private fun loadProductsFromAssets(cacheAndLog: Boolean): List<Product> {
        return try {
            val json = context.assets.open("products.json").bufferedReader().use { it.readText() }
            val type = object : TypeToken<List<Product>>() {}.type
            val localProducts: List<Product> = Gson().fromJson(json, type)
            if (cacheAndLog) {
                saveProductsToCache(localProducts)
                Log.d("ProductRepository", "Loaded data from Local JSON: ${localProducts.size} items")
            }
            localProducts
        } catch (e: Exception) {
            Log.e("ProductRepository", "Failed to load Local JSON", e)
            emptyList()
        }
    }

    private fun saveProductsToCache(products: List<Product>) {
        this.products.clear()
        this.products.addAll(products)
    }

    fun getProductById(id: Int?): Product? {
        if (id == null) return null
        return products.find { it.id == id }
    }
}
