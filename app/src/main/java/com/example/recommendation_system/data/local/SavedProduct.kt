package com.example.recommendation_system.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "saved_products")
data class SavedProduct(
    @PrimaryKey val asin: String,
    val title: String,
    val price: Double,
    val category: String,
    val score: Double?,
    val isLowPrice: Boolean = false
)
