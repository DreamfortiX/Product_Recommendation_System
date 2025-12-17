package com.example.recommendation_system

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.recommendation_system.data.api.RetrofitClient
import com.example.recommendation_system.data.model.ProductInfo
import com.example.recommendation_system.databinding.ActivityProductDetailBinding
import kotlinx.coroutines.launch


class ProductDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProductDetailBinding
    private var currentAsin: String? = null
    private var productTitle: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProductDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Get product data from intent
        currentAsin = intent.getStringExtra("asin")
        productTitle = intent.getStringExtra("title")

        setupUI()

        if (currentAsin != null) {
            loadProductDetails()
        } else {
            Toast.makeText(this, "No product selected", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun setupUI() {
        // Setup toolbar
        binding.toolbar.title = productTitle ?: "Product Details"
        binding.toolbar.setNavigationOnClickListener { finish() }

        // Setup button listeners
        binding.btnGetRecommendations.setOnClickListener {
            if (currentAsin != null) {
                val intent = Intent(this, RecommendationActivity::class.java)
                intent.putExtra("asin", currentAsin)
                intent.putExtra("product_title", productTitle)
                startActivity(intent)
            }
        }

        binding.btnShare.setOnClickListener {
            shareProduct()
        }

        binding.btnCompare.setOnClickListener {
            Toast.makeText(this, "Compare feature coming soon", Toast.LENGTH_SHORT).show()
        }

        // Setup tab layout
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText("Details"))
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText("Reviews"))
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText("Similar"))

        binding.tabLayout.addOnTabSelectedListener(object : com.google.android.material.tabs.TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: com.google.android.material.tabs.TabLayout.Tab?) {
                when (tab?.position) {
                    0 -> showDetailsTab()
                    1 -> showReviewsTab()
                    2 -> showSimilarTab()
                }
            }

            override fun onTabUnselected(tab: com.google.android.material.tabs.TabLayout.Tab?) {}
            override fun onTabReselected(tab: com.google.android.material.tabs.TabLayout.Tab?) {}
        })

        // Setup rating bar
        binding.ratingBar.setOnRatingBarChangeListener { _, rating, _ ->
            Toast.makeText(this, "Rated: $rating", Toast.LENGTH_SHORT).show()
        }

        // Setup swipe refresh
        binding.swipeRefresh.setOnRefreshListener {
            loadProductDetails()
        }
    }

    private fun loadProductDetails() {
        lifecycleScope.launch {
            try {
                binding.progressBar.visibility = View.VISIBLE
                binding.errorView.visibility = View.GONE

                val response = RetrofitClient.apiService.getProductDetails(currentAsin!!)

                if (response.isSuccessful && response.body()?.success == true) {
                    val product = response.body()?.product
                    product?.let {
                        updateUI(it)
                        binding.content.visibility = View.VISIBLE
                        binding.errorView.visibility = View.GONE
                    }
                } else {
                    showError("Failed to load product details")
                }
            } catch (e: Exception) {
                showError("Error: ${e.message}")
            } finally {
                binding.progressBar.visibility = View.GONE
                binding.swipeRefresh.isRefreshing = false
            }
        }
    }

    private fun updateUI(product: ProductInfo) {
        // Update basic info
        binding.toolbar.title = product.title
        binding.tvProductTitle.text = product.title
        binding.tvCategory.text = product.category
        binding.tvPrice.text = "$${product.price}"
        binding.tvRating.text = "${product.stars}/5.0"

        // Update rating bar
        binding.ratingBar.rating = product.stars.toFloat()

        // Update star distribution (simulated)
        updateStarDistribution(product.stars)

        // Update description
        product.description?.let {
            binding.tvDescription.text = it
        }

        // Update reviews count
        product.reviews?.let {
            binding.tvReviewsCount.text = "$it reviews"
            binding.tvTotalReviews.text = "Based on $it customer reviews"
        }

        // Update recommendation score
        updateRecommendationScore(product)
    }

    private fun updateStarDistribution(rating: Double) {
        // Simulated star distribution
        val distribution = mapOf(
            5 to 65,
            4 to 20,
            3 to 10,
            2 to 3,
            1 to 2
        )

        binding.progress5Star.progress = distribution[5] ?: 0
        binding.progress4Star.progress = distribution[4] ?: 0
        binding.progress3Star.progress = distribution[3] ?: 0
        binding.progress2Star.progress = distribution[2] ?: 0
        binding.progress1Star.progress = distribution[1] ?: 0

        binding.tv5StarPercent.text = "${distribution[5]}%"
        binding.tv4StarPercent.text = "${distribution[4]}%"
        binding.tv3StarPercent.text = "${distribution[3]}%"
        binding.tv2StarPercent.text = "${distribution[2]}%"
        binding.tv1StarPercent.text = "${distribution[1]}%"
    }

    private fun updateRecommendationScore(product: ProductInfo) {
        // Calculate a recommendation score based on product features
        var score = 0.0

        // Score based on rating
        score += (product.stars / 5.0) * 40

        // Score based on reviews (if available)
        product.reviews?.let {
            val reviewScore = when {
                it > 1000 -> 30
                it > 500 -> 25
                it > 100 -> 20
                it > 50 -> 15
                else -> 10
            }
            score += reviewScore
        }

        // Score based on price (lower price gets higher score)
        val priceScore = when {
            product.price < 10 -> 30
            product.price < 50 -> 25
            product.price < 100 -> 20
            product.price < 200 -> 15
            else -> 10
        }
        score += priceScore

        // Update UI
        val finalScore = score.toInt()
        binding.progressRecommendationScore.progress = finalScore
        binding.tvRecommendationScore.text = "$finalScore%"

        // Set score description
        val description = when {
            finalScore >= 80 -> "Highly Recommended"
            finalScore >= 60 -> "Recommended"
            finalScore >= 40 -> "Average"
            else -> "Not Recommended"
        }
        binding.tvScoreDescription.text = description
    }

    private fun showDetailsTab() {
        binding.detailsTab.visibility = View.VISIBLE
        binding.reviewsTab.visibility = View.GONE
        binding.similarTab.visibility = View.GONE
    }

    private fun showReviewsTab() {
        binding.detailsTab.visibility = View.GONE
        binding.reviewsTab.visibility = View.VISIBLE
        binding.similarTab.visibility = View.GONE

        // Load reviews (simulated)
        loadSimulatedReviews()
    }

    private fun showSimilarTab() {
        binding.detailsTab.visibility = View.GONE
        binding.reviewsTab.visibility = View.GONE
        binding.similarTab.visibility = View.VISIBLE

        // Load similar products
        loadSimilarProducts()
    }

    private fun loadSimulatedReviews() {
        // Simulated reviews data
        val reviews = listOf(
            SimulatedReview("John D.", 5.0, "2 months ago", "Excellent product! Highly recommended.", true),
            SimulatedReview("Sarah M.", 4.0, "1 month ago", "Good quality but a bit expensive.", false),
            SimulatedReview("Mike T.", 3.0, "3 weeks ago", "Average product. Does the job.", false),
            SimulatedReview("Emma L.", 5.0, "2 weeks ago", "Best purchase I've made this year!", true),
            SimulatedReview("David K.", 2.0, "1 week ago", "Not as described. Disappointed.", false)
        )

        // Clear previous reviews
        binding.reviewsContainer.removeAllViews()

        // Add review views
        reviews.forEach { review ->
            val reviewView = layoutInflater.inflate(R.layout.item_review, null)

            val tvName = reviewView.findViewById<android.widget.TextView>(R.id.tvReviewerName)
            val tvDate = reviewView.findViewById<android.widget.TextView>(R.id.tvReviewDate)
            val tvContent = reviewView.findViewById<android.widget.TextView>(R.id.tvReviewContent)
            val ratingBar = reviewView.findViewById<android.widget.RatingBar>(R.id.ratingBarReview)
            val ivVerified = reviewView.findViewById<android.widget.ImageView>(R.id.ivVerified)

            tvName.text = review.reviewerName
            tvDate.text = review.date
            tvContent.text = review.content
            ratingBar.rating = review.rating.toFloat()
            ivVerified.visibility = if (review.verified) View.VISIBLE else View.GONE

            binding.reviewsContainer.addView(reviewView)
        }
    }

    private fun loadSimilarProducts() {
        // Simulated similar products
        val similarProducts = listOf(
            SimilarProduct("Wireless Headphones", "$79.99", 4.5, "Electronics"),
            SimilarProduct("Bluetooth Speaker", "$49.99", 4.2, "Electronics"),
            SimilarProduct("Phone Case", "$19.99", 4.0, "Accessories"),
            SimilarProduct("Screen Protector", "$9.99", 3.8, "Accessories"),
            SimilarProduct("USB Cable", "$12.99", 4.1, "Electronics")
        )

        // Clear previous products
        binding.similarProductsContainer.removeAllViews()

        // Add product views
        similarProducts.forEach { product ->
            val productView = layoutInflater.inflate(R.layout.item_similar_product, null)

            val tvTitle = productView.findViewById<android.widget.TextView>(R.id.tvProductTitle)
            val tvPrice = productView.findViewById<android.widget.TextView>(R.id.tvProductPrice)
            val tvCategory = productView.findViewById<android.widget.TextView>(R.id.tvProductCategory)
            val ratingBar = productView.findViewById<android.widget.RatingBar>(R.id.ratingBarProduct)

            tvTitle.text = product.title
            tvPrice.text = product.price
            tvCategory.text = product.category
            ratingBar.rating = product.rating.toFloat()

            // Set click listener
            productView.setOnClickListener {
                Toast.makeText(this, "Selected: ${product.title}", Toast.LENGTH_SHORT).show()
            }

            binding.similarProductsContainer.addView(productView)
        }
    }

    private fun shareProduct() {
        val shareIntent = Intent(Intent.ACTION_SEND)
        shareIntent.type = "text/plain"
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Check out this product")
        shareIntent.putExtra(Intent.EXTRA_TEXT,
            "Check out this product: ${productTitle}\n" +
                    "Category: ${binding.tvCategory.text}\n" +
                    "Price: ${binding.tvPrice.text}\n" +
                    "Rating: ${binding.tvRating.text}"
        )
        startActivity(Intent.createChooser(shareIntent, "Share via"))
    }

    private fun showError(message: String) {
        binding.content.visibility = View.GONE
        binding.errorView.visibility = View.VISIBLE
        binding.tvError.text = message

        binding.btnRetry.setOnClickListener {
            loadProductDetails()
        }
    }

    // Data classes for simulated data
    data class SimulatedReview(
        val reviewerName: String,
        val rating: Double,
        val date: String,
        val content: String,
        val verified: Boolean
    )

    data class SimilarProduct(
        val title: String,
        val price: String,
        val rating: Double,
        val category: String
    )
}