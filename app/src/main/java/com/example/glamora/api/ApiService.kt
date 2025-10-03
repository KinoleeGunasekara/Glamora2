package com.example.glamora.api

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ApiService {
    private val retrofit = Retrofit.Builder()
        .baseUrl("https://fakestoreapi.com/") // Base URL
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val productApi: ProductApi = retrofit.create(ProductApi::class.java)
}
