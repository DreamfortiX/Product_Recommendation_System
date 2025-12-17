package com.example.recommendation_system

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.lifecycleScope
import com.example.recommendation_system.data.api.RetrofitClient
import com.example.recommendation_system.databinding.ActivityMainBinding
import kotlinx.coroutines.launch
import androidx.core.content.edit


class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        hideStatusBar()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupUI()
        checkApiConnection()
    }

    private fun setupUI() {
        binding.btnSearch.setOnClickListener {
            val query = binding.etSearch.text.toString().trim()
            if (query.isNotEmpty()) {
                val intent = Intent(this, SearchActivity::class.java)
                intent.putExtra("query", query)
                startActivity(intent)
            } else {
                Toast.makeText(this, "Please enter a search term", Toast.LENGTH_SHORT).show()
            }
        }

//        binding.chipCategories.setOnClickListener {
//            // Show categories dialog
//            showCategoriesDialog()
//        }
//
//        binding.chipMethods.setOnClickListener {
//            // Show recommendation methods
//            val intent = Intent(this, RecommendationActivity::class.java)
//            startActivity(intent)
//        }

        // Logout button
        binding.btnProfile.setOnClickListener {


            // Clear local login flag
            getSharedPreferences("app_prefs", MODE_PRIVATE)
                .edit {
                    putBoolean("user_logged_in", false)
                }

            // Go to Login
            val intent = Intent(this,LoginActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            startActivity(intent)
            finish()
        }
    }

    @SuppressLint("SetTextI18n")
    private fun checkApiConnection() {
        lifecycleScope.launch {
            try {
                binding.progressBar.visibility = View.VISIBLE
                val response = RetrofitClient.apiService.getHealth()
                if (response.isSuccessful) {
                    val health = response.body()
                    binding.tvStatus.text = "API Status: ${health?.status ?: "Unknown"}"
                    binding.tvProductsCount.text = "Products: ${health?.totalProducts ?: 0}"
                } else {
                    binding.tvStatus.text = "API Status: Unreachable"
                    Toast.makeText(
                        this@MainActivity,
                        "Cannot connect to server. Make sure FastAPI is running.",
                        Toast.LENGTH_LONG
                    ).show()
                }
            } catch (e: Exception) {
                binding.tvStatus.text = "API Status: Error"
                Toast.makeText(
                    this@MainActivity,
                    "Connection error: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            } finally {
                binding.progressBar.visibility = View.GONE
            }
        }
    }

    private fun hideStatusBar() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        val controller = WindowInsetsControllerCompat(window, window.decorView)
        controller.hide(WindowInsetsCompat.Type.statusBars())
        controller.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
    }
    private fun showCategoriesDialog() {
        // Implement categories dialog
        Toast.makeText(this, "Categories feature coming soon", Toast.LENGTH_SHORT).show()
    }
}