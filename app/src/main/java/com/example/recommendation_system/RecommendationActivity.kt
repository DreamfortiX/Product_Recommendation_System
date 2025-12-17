package com.example.recommendation_system


import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.coroutines.launch
import androidx.lifecycle.lifecycleScope
import com.example.recommendation_system.Adapters.RecommendationAdapter
import com.example.recommendation_system.data.api.RetrofitClient
import com.example.recommendation_system.data.model.Product
import com.example.recommendation_system.data.model.ProductInfo
import com.example.recommendation_system.data.model.RecommendationRequest
import com.example.recommendation_system.databinding.ActivityRecommendationBinding
import java.net.SocketTimeoutException
import java.net.UnknownHostException

class RecommendationActivity : AppCompatActivity(), RecommendationAdapter.OnRecommendationClickListener {

    private lateinit var binding: ActivityRecommendationBinding
    private lateinit var adapter: RecommendationAdapter

    private var currentMethod = "hybrid"
    private var currentAsin: String? = null
    private var productInfo: ProductInfo? = null

    companion object {
        private const val TAG = "RecommendationActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRecommendationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        Log.d(TAG, "RecommendationActivity created")

        currentAsin = intent.getStringExtra("asin")
        val productTitle = intent.getStringExtra("product_title")

        Log.d(TAG, "ASIN received: $currentAsin")
        Log.d(TAG, "Title received: $productTitle")

        setupUI(productTitle)
        setupRecyclerView()
        setupMethodButtons()

        if (currentAsin != null && currentAsin!!.isNotEmpty()) {
            loadRecommendations()
        } else {
            Log.e(TAG, "No ASIN provided!")
            showError("No product selected", "Please select a product first")
            binding.btnRetry.visibility = View.GONE
            binding.btnTestConnection.visibility = View.GONE
        }
    }

    private fun setupUI(productTitle: String?) {
        binding.toolbar.title = productTitle ?: "Recommendations"
        binding.toolbar.setNavigationOnClickListener { finish() }

        binding.tvProductTitle.text = productTitle ?: "Loading..."

        binding.swipeRefresh.setOnRefreshListener {
            loadRecommendations()
        }

        binding.fabRefresh.setOnClickListener {
            loadRecommendations()
        }

        // Setup error view buttons
        binding.btnRetry.setOnClickListener {
            loadRecommendations()
        }

        binding.btnTestConnection.setOnClickListener {
            testConnection()
        }

        binding.btnCompare.setOnClickListener {
            val selectedProducts = adapter.getSelectedProducts()

            if (selectedProducts.size < 2) {
                Toast.makeText(this, "Select at least 2 products to compare", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (selectedProducts.size > 3) {
                Toast.makeText(this, "You can compare up to 3 products", Toast.LENGTH_SHORT).show()
            }

            val productsToCompare = selectedProducts.take(3)

            val intent = Intent(this, CompareProductsActivity::class.java)
            intent.putExtra(CompareProductsActivity.EXTRA_PRODUCTS, ArrayList(productsToCompare))
            startActivity(intent)
        }
    }

    private fun setupRecyclerView() {
        adapter = RecommendationAdapter(this)
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter
    }

    private fun setupMethodButtons() {
        val methodButtons = listOf(
            binding.btnCosine to "cosine",
            binding.btnDecisionTree to "decision_tree",
            binding.btnGnn to "gnn",
            binding.btnHybrid to "hybrid"
        )

        methodButtons.forEach { (button, method) ->
            button.setOnClickListener {
                currentMethod = method
                updateMethodSelection()
                loadRecommendations()
            }
        }

        updateMethodSelection()
    }

    private fun updateMethodSelection() {
        binding.btnCosine.isSelected = currentMethod == "cosine"
        binding.btnDecisionTree.isSelected = currentMethod == "decision_tree"
        binding.btnGnn.isSelected = currentMethod == "gnn"
        binding.btnHybrid.isSelected = currentMethod == "hybrid"

        val description = when (currentMethod) {
            "cosine" -> "Content-based similarity using text features"
            "decision_tree" -> "Feature-based recommendations using machine learning"
            "gnn" -> "Graph-based recommendations using relationships"
            else -> "Combination of all methods for best results"
        }
        binding.tvMethodDescription.text = description
    }

    private fun loadRecommendations() {
        currentAsin?.let { asin ->
            lifecycleScope.launch {
                try {
                    Log.d(TAG, "Loading recommendations for ASIN: $asin")
                    Log.d(TAG, "Using method: $currentMethod")

                    showLoading(true)
                    hideError()
                    hideEmptyState()
                    hideRecyclerView()

                    val response = RetrofitClient.apiService.getRecommendations(
                        RecommendationRequest(
                            asin = asin,
                            method = currentMethod,
                            top_n = 10
                        )
                    )

                    Log.d(TAG, "API Response received: ${response.isSuccessful}")
                    Log.d(TAG, "Response code: ${response.code()}")

                    if (response.isSuccessful) {
                        val recommendationResponse = response.body()

                        if (recommendationResponse?.success == true) {
                            // Update product info
                            productInfo = recommendationResponse.productInfo
                            productInfo?.let {
                                binding.tvProductTitle.text = it.title
                                binding.tvProductDetails.text = buildString {
                                    append("Category: ${it.category}\n")
                                    append("Price: $${String.format("%.2f", it.price)}\n")
                                    append("Rating: ${it.stars}/5.0")
                                    it.reviews?.let { reviews ->
                                        append("\nReviews: ${reviews.formatWithCommas()}")
                                    }
                                }
                            }

                            // Update recommendations
                            if (recommendationResponse.recommendations.isNotEmpty()) {
                                Log.d(TAG, "Received ${recommendationResponse.recommendations.size} recommendations")
                                adapter.submitList(recommendationResponse.recommendations)
                                showRecyclerView()
                                binding.btnCompare.visibility = View.VISIBLE
                                hideEmptyState()
                                hideError()
                            } else {
                                Log.d(TAG, "No recommendations in response")
                                adapter.submitList(emptyList())
                                showEmptyState("No recommendations found using ${currentMethod.replace("_", " ")} method")
                                hideRecyclerView()
                                binding.btnCompare.visibility = View.GONE
                                hideError()
                            }
                        } else {
                            val errorMsg = recommendationResponse?.message ?: "Unknown error"
                            Log.e(TAG, "API returned error: $errorMsg")
                            showError("API Error", errorMsg)
                        }
                    } else {
                        Log.e(TAG, "API call failed: ${response.code()} - ${response.message()}")
                        val errorMsg = when (response.code()) {
                            404 -> "Product not found (404)"
                            500 -> "Server error (500)"
                            503 -> "Service unavailable (503)"
                            else -> "Server error: ${response.code()}"
                        }
                        showError("Server Error", errorMsg)
                    }
                } catch (e: SocketTimeoutException) {
                    Log.e(TAG, "Timeout error", e)
                    showError("Connection Timeout",
                        "Server is taking too long to respond.\n\n" +
                                "Please check:\n" +
                                "• Your internet connection\n" +
                                "• Server is running\n" +
                                "• Try a different method"
                    )
                } catch (e: UnknownHostException) {
                    Log.e(TAG, "Connection error", e)
                    showError("Connection Failed",
                        "Cannot connect to server.\n\n" +
                                "Server URL: ${RetrofitClient.getBaseUrl()}\n\n" +
                                "Please check:\n" +
                                "• Server is running\n" +
                                "• Correct IP address\n" +
                                "• Same WiFi network"
                    )
                } catch (e: Exception) {
                    Log.e(TAG, "Error loading recommendations", e)
                    showError("Error", e.message ?: "Unknown error occurred")
                } finally {
                    showLoading(false)
                    binding.swipeRefresh.isRefreshing = false
                }
            }
        } ?: run {
            showError("No Product Selected", "Please go back and select a product first")
            binding.btnRetry.visibility = View.GONE
        }
    }

    private fun testConnection() {
        lifecycleScope.launch {
            try {
                showLoading(true)
                val response = RetrofitClient.apiService.getHealth()

                if (response.isSuccessful) {
                    val health = response.body()
                    val message = """
                        ✅ Connection Successful!
                        
                        Status: ${health?.status}
                        Version: ${health?.version}
                        Products: ${health?.totalProducts}
                        
                        Server: ${RetrofitClient.getBaseUrl()}
                    """.trimIndent()

                    showError("Connection Test", message)
                    Toast.makeText(this@RecommendationActivity, "✅ Connected!", Toast.LENGTH_LONG).show()
                } else {
                    showError("Connection Test Failed",
                        "Status: ${response.code()}\n" +
                                "Message: ${response.message()}\n\n" +
                                "Server: ${RetrofitClient.getBaseUrl()}"
                    )
                }
            } catch (e: Exception) {
                showError("Connection Test Error",
                    "Error: ${e.message}\n\n" +
                            "Server: ${RetrofitClient.getBaseUrl()}"
                )
            } finally {
                showLoading(false)
            }
        }
    }

    private fun showLoading(show: Boolean) {
        binding.progressBar.visibility = if (show) View.VISIBLE else View.GONE
        if (show) {
            // Hide other views when loading
            binding.errorView.visibility = View.GONE
            binding.tvEmpty.visibility = View.GONE
            binding.recyclerView.visibility = View.GONE
        }
    }

    private fun showError(title: String, message: String) {
        binding.tvError.text = title
        binding.tvErrorDetails.text = message
        binding.errorView.visibility = View.VISIBLE
        binding.tvEmpty.visibility = View.GONE
        binding.recyclerView.visibility = View.GONE
        binding.btnRetry.visibility = View.VISIBLE
        binding.btnTestConnection.visibility = View.VISIBLE
    }

    private fun hideError() {
        binding.errorView.visibility = View.GONE
    }

    private fun showEmptyState(message: String) {
        binding.tvEmpty.text = message
        binding.tvEmpty.visibility = View.VISIBLE
        binding.errorView.visibility = View.GONE
        binding.recyclerView.visibility = View.GONE
    }

    private fun hideEmptyState() {
        binding.tvEmpty.visibility = View.GONE
    }

    private fun showRecyclerView() {
        binding.recyclerView.visibility = View.VISIBLE
    }

    private fun hideRecyclerView() {
        binding.recyclerView.visibility = View.GONE
    }

    // Extension function for formatting numbers
    private fun Int.formatWithCommas(): String {
        return String.format("%,d", this)
    }

    override fun onRecommendationClick(product: Product) {
        Log.d(TAG, "Recommendation clicked: ${product.title}")

        val intent = Intent(this, ProductDetailActivity::class.java)
        intent.putExtra("asin", product.asin)
        intent.putExtra("title", product.title)
        startActivity(intent)
    }

    override fun onViewDetailsClick(product: Product) {
        onRecommendationClick(product)
    }
}