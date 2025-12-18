package com.example.recommendation_system.data.local

import com.example.recommendation_system.data.model.Product

fun Product.toSavedProduct(isLowPrice: Boolean = false): SavedProduct {
    return SavedProduct(
        asin = this.asin,
        title = this.title,
        price = this.price,
        category = this.category,
        score = this.score,
        isLowPrice = isLowPrice
    )
}

fun List<Product>.toSavedProducts(isLowPrice: Boolean = false): List<SavedProduct> {
    return this.map { it.toSavedProduct(isLowPrice) }
}
