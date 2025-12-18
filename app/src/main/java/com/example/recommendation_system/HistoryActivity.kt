package com.example.recommendation_system

import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.recommendation_system.Adapters.SavedProductsAdapter
import com.example.recommendation_system.data.local.AppDatabase
import com.example.recommendation_system.data.local.SavedProduct
import com.google.android.material.appbar.MaterialToolbar
import kotlinx.coroutines.launch

class HistoryActivity : AppCompatActivity() {

    private lateinit var recycler: RecyclerView
    private lateinit var adapter: SavedProductsAdapter
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private lateinit var emptyState: View
    private lateinit var toolbar: MaterialToolbar
    private lateinit var db: AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_history)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Initialize views
        recycler = findViewById(R.id.recyclerSaved)
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout)
        emptyState = findViewById(R.id.emptyState)
        toolbar = findViewById(R.id.toolbar)

        // Initialize database
        db = AppDatabase.getInstance(this)

        // Setup toolbar
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Saved Products"

        // Setup adapter
        adapter = SavedProductsAdapter()
        recycler.layoutManager = LinearLayoutManager(this)
        recycler.adapter = adapter

        // Setup swipe refresh
        setupSwipeRefresh()

        // Load initial data
        loadSavedProducts()
    }

    private fun setupSwipeRefresh() {
        // Set refresh colors (you can define these in colors.xml)
        swipeRefreshLayout.setColorSchemeResources(
            R.color.colorPrimary,  // Add these colors to your colors.xml
            R.color.colorAccent,   // or use android.R.color.holo_blue_bright, etc.
            R.color.colorPrimaryDark
        )

        swipeRefreshLayout.setOnRefreshListener {
            // Refresh data
            loadSavedProducts()
        }
    }

    private fun loadSavedProducts() {
        lifecycleScope.launch {
            val items = db.savedProductDao().getAll()

            // Update UI on main thread
            runOnUiThread {
                if (items.isEmpty()) {
                    showEmptyState()
                } else {
                    showProductList(items)
                }

                // Stop refreshing animation
                swipeRefreshLayout.isRefreshing = false
            }
        }
    }

    private fun showEmptyState() {
        recycler.visibility = View.GONE
        emptyState.visibility = View.VISIBLE
    }

    private fun showProductList(items: List<SavedProduct>) {
        recycler.visibility = View.VISIBLE
        emptyState.visibility = View.GONE
        adapter.submitList(items)
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

//    // Optional: Handle browse products button click
//    // Add this if you have a button in your empty state
//    private fun setupEmptyStateButton() {
//        val browseButton = findViewById<com.google.android.material.button.MaterialButton>(R.id.btnBrowseProducts)
//        browseButton?.setOnClickListener {
//            // Navigate back to product listing or main activity
//            finish()
//        }
//    }
}