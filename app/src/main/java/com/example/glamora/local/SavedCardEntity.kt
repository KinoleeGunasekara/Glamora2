package com.example.glamora.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

/**
 * SavedCardEntity stores user's saved payment method details in local database.
 * Only saves non-sensitive information for display and convenience purposes.
 * Security: CVV is never stored, only last 4 digits of card number are kept.
 */
@Entity(tableName = "saved_cards")
data class SavedCardEntity(
    @PrimaryKey(autoGenerate = true)
    val cardId: Int = 0, // Auto-generated unique identifier for each saved card

    val cardHolderName: String, // User's name as it appears on the card
    val lastFourDigits: String, // Only last 4 digits for security (e.g., "1234")
    val expiryDate: String, // MM/YYYY format for display purposes
    val cardType: String, // VISA, MASTERCARD, or RUPAY for showing appropriate icon
    val isDefault: Boolean = false, // Marks one card as user's preferred payment method
    val dateAdded: Date = Date() // Timestamp when card was saved for sorting purposes
)
