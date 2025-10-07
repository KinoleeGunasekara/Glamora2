package com.example.glamora.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "profile")
data class ProfileEntity(
    @PrimaryKey val id: Int = 0, // always single profile
    val name: String,
    val email: String,
    val photoPath: String? // path to saved profile photo
)
