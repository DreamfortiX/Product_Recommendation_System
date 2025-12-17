package com.example.recommendation_system.data.api



import com.example.recommendation_system.data.model.HealthResponse
import com.example.recommendation_system.data.model.ProductInfo
import com.example.recommendation_system.data.model.RecommendationRequest
import com.example.recommendation_system.data.model.RecommendationResponse
import com.example.recommendation_system.data.model.SearchRequest
import com.example.recommendation_system.data.model.SearchResponse
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    @GET("health")
    suspend fun getHealth(): Response<HealthResponse>

    @POST("recommend")
    suspend fun getRecommendations(
        @Body request: RecommendationRequest
    ): Response<RecommendationResponse>

    @POST("search")
    suspend fun searchProducts(
        @Body request: SearchRequest
    ): Response<SearchResponse>

    @GET("product/{asin}")
    suspend fun getProductDetails(
        @Path("asin") asin: String
    ): Response<ProductDetailResponse>

    @GET("methods")
    suspend fun getMethods(): Response<MethodsResponse>
}

data class ProductDetailResponse(
    val success: Boolean,
    val product: ProductInfo
)

data class MethodsResponse(
    val methods: List<RecommendationMethod>
)

data class RecommendationMethod(
    val id: String,
    val name: String,
    val description: String
)