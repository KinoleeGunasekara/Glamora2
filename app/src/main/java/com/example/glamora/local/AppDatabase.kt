package com.example.glamora.local

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import android.content.Context
import java.util.Date

/**
 * TypeConverters class handles conversion between Java Date objects and Long values.
 * Room can't directly store Date objects, so we convert them to timestamps.
 * This is required for storing orderDate in OrderEntity.
 */
class Converters {
    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }
}

/**
 * Updated database schema to include order-related tables and saved payment cards.
 * Version incremented to 4 to handle new saved_cards table.
 * Added TypeConverters to handle Date serialization.
 * fallbackToDestructiveMigration() will recreate database on version change.
 */
@Database(
    entities = [CartItemEntity::class, ProfileEntity::class, OrderEntity::class, OrderItemEntity::class, SavedCardEntity::class],
    version = 4, // Incremented from 3 to handle new saved_cards table
    exportSchema = false
)
@TypeConverters(Converters::class) // Enable Date conversion throughout the database
abstract class AppDatabase : RoomDatabase() {
    abstract fun cartDao(): CartDao
    abstract fun profileDao(): ProfileDao
    abstract fun orderDao(): OrderDao // New DAO for order operations
    abstract fun savedCardDao(): SavedCardDao // New DAO for saved payment cards

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "glamora_database"
                )
                    .fallbackToDestructiveMigration() // Recreates database on version change (data loss acceptable for new feature)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
