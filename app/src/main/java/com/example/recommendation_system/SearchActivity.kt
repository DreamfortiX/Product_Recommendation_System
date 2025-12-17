package com.example.recommendation_system

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.recommendation_system.Adapters.ProductAdapter
import com.example.recommendation_system.data.api.RetrofitClient
import com.example.recommendation_system.data.model.Product
import com.example.recommendation_system.data.model.SearchRequest
import com.example.recommendation_system.databinding.ActivitySearchBinding
import kotlinx.coroutines.launch

class SearchActivity : AppCompatActivity(), ProductAdapter.OnProductClickListener {
    private lateinit var binding: ActivitySearchBinding
    private lateinit var adapter: ProductAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySearchBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupUI()
        setupRecyclerView()

        val query = intent.getStringExtra("query")
        if (!query.isNullOrEmpty()) {
            binding.etSearch.setText(query)
            searchProducts(query)
        }
    }

    private fun setupUI() {
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }

        binding.btnSearch.setOnClickListener {
            val query = binding.etSearch.text.toString().trim()
            if (query.isNotEmpty()) {
                searchProducts(query)
            } else {
                Toast.makeText(this, "Please enter a search term", Toast.LENGTH_SHORT).show()
            }
        }

        binding.swipeRefresh.setOnRefreshListener {
            val query = binding.etSearch.text.toString().trim()
            if (query.isNotEmpty()) {
                searchProducts(query)
            } else {
                binding.swipeRefresh.isRefreshing = false
            }
        }

        // Set up search text field
        binding.etSearch.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                val query = binding.etSearch.text.toString().trim()
                if (query.isNotEmpty()) {
                    searchProducts(query)
                    return@setOnEditorActionListener true
                }
            }
            false
        }
    }

    private fun setupRecyclerView() {
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = ProductAdapter(this)
        binding.recyclerView.adapter = adapter
    }

    private fun searchProducts(query: String) {
        lifecycleScope.launch {
            try {
                showLoading(true)
                hideEmptyState()

                val response = RetrofitClient.apiService.searchProducts(
                    SearchRequest(query = query, limit = 20)
                )

                if (response.isSuccessful) {
                    val searchResponse = response.body()
                    if (searchResponse?.success == true && searchResponse.products.isNotEmpty()) {
                        adapter.submitList(searchResponse.products)
                        showResultsCount(searchResponse.products.size)
                        binding.recyclerView.visibility = View.VISIBLE
                        hideEmptyState()
                    } else {
                        adapter.submitList(emptyList())
                        showEmptyState("No products found for '$query'")
                        binding.recyclerView.visibility = View.GONE
                    }
                } else {
                    showEmptyState("Search failed: ${response.message()}")
                    binding.recyclerView.visibility = View.GONE
                }
            } catch (e: Exception) {
                showEmptyState("Error: ${e.message}")
                binding.recyclerView.visibility = View.GONE
            } finally {
                showLoading(false)
                binding.swipeRefresh.isRefreshing = false
            }
        }
    }

    private fun showLoading(show: Boolean) {
        binding.progressBar.visibility = if (show) View.VISIBLE else View.GONE
    }

    private fun showEmptyState(message: String) {
        binding.tvEmpty.text = message
        binding.tvEmpty.visibility = View.VISIBLE
    }

    private fun hideEmptyState() {
        binding.tvEmpty.visibility = View.GONE
    }

    private fun showResultsCount(count: Int) {
        binding.tvResultsCount.text = "Found $count product${if (count != 1) "s" else ""}"
        binding.tvResultsCount.visibility = View.VISIBLE
    }

    // ProductAdapter.OnProductClickListener implementation
    override fun onProductClick(product: Product) {
        // Navigate to product detail
        val intent = Intent(this, ProductDetailActivity::class.java)
        intent.putExtra("asin", product.asin)
        intent.putExtra("title", product.title)
        startActivity(intent)
    }

    override fun onGetRecommendationsClick(product:Product) {
        // Navigate to recommendations
        val intent = Intent(this, RecommendationActivity::class.java)
        intent.putExtra("asin", product.asin)
        intent.putExtra("product_title", product.title)
        startActivity(intent)
    }
}