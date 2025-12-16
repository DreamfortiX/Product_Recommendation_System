package com.example.choice.data.model
import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class Product(
    @SerializedName("asin") val asin: String,
    @SerializedName("title") val title: String,
    @SerializedName("category") val category: String,
    @SerializedName("stars") val stars: Double,
    @SerializedName("price") val price: Double,
    @SerializedName("method") val method: String? = null,
    @SerializedName("score") val score: Double? = null,
    @SerializedName("description") val description: String? = null
) : Serializable

data class ProductInfo(
    @SerializedName("asin") val asin: String,
    @SerializedName("title") val title: String,
    @SerializedName("category") val category: String,
    @SerializedName("stars") val stars: Double,
    @SerializedName("price") val price: Double,
    @SerializedName("reviews") val reviews: Int? = null,
    @SerializedName("description") val description: String? = null
)

data class RecommendationRequest(
    val asin: String? = null,
    val query: String? = null,
    val method: String = "hybrid",
    val top_n: Int = 10
)

data class RecommendationResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String,
    @SerializedName("recommendations") val recommendations: List<Product>,
    @SerializedName("product_info") val productInfo: ProductInfo? = null
)

data class SearchResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String,
    @SerializedName("products") val products: List<Product>
)

data class HealthResponse(
    @SerializedName("status") val status: String,
    @SerializedName("version") val version: String,
    @SerializedName("models_loaded") val modelsLoaded: Map<String, Boolean>,
    @SerializedName("total_products") val totalProducts: Int
)

data class SearchRequest(
    val query: String,
    val limit: Int = 20
)
