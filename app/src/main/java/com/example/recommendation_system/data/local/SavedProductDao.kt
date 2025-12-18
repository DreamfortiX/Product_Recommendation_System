package com.example.recommendation_system.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface SavedProductDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(product: SavedProduct)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(products: List<SavedProduct>)

    @Query("SELECT * FROM saved_products ORDER BY isLowPrice DESC, price ASC")
    suspend fun getAll(): List<SavedProduct>

    @Query("SELECT * FROM saved_products WHERE asin = :asin LIMIT 1")
    suspend fun getByAsin(asin: String): SavedProduct?

    @Delete
    suspend fun delete(product: SavedProduct)

    @Query("DELETE FROM saved_products")
    suspend fun clear()
}
