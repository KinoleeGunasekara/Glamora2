package com.example.glamora.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.glamora.repository.CartRepository

// This factory takes the repository as a parameter
class CartViewModelFactory(private val repository: CartRepository) : ViewModelProvider.Factory {

    // This function creates the CartViewModel instance
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CartViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CartViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
