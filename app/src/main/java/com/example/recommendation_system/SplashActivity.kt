package com.example.recommendation_system

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import com.airbnb.lottie.LottieAnimationView
import androidx.appcompat.app.AppCompatActivity
import com.example.recommendation_system.databinding.ActivitySplashBinding
import kotlin.jvm.java

@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {

    private lateinit var progressBar: ProgressBar
    private lateinit var progressText: TextView
    private lateinit var stepText: TextView
    private lateinit var loadingText: TextView
    private lateinit var binding: ActivitySplashBinding

    private var progressStatus = 0
    private val handler = Handler(Looper.getMainLooper())

    // Simulated loading steps
    private val loadingSteps = listOf(
        "Loading dataset...",
        "Initializing Cosine Similarity...",
        "Setting up Decision Tree...",
        "Configuring GNN models...",
        "Building recommendation engine...",
        "Almost ready..."
    )
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize views
        progressBar = findViewById(R.id.progressBar)
        progressText = findViewById(R.id.progressText)
        stepText = findViewById(R.id.stepText)
        loadingText = findViewById(R.id.loadingText)

        // Set up Lottie animation
        val lottieAnimation = findViewById<LottieAnimationView>(R.id.lottieAnimationView)
        lottieAnimation.playAnimation()

        // Start progress animation
        startLoadingAnimation()
    }

    private fun startLoadingAnimation() {
        // Create a thread to update progress
        Thread(Runnable {
            while (progressStatus < 100) {
                progressStatus += 1

                // Update UI on main thread
                handler.post {
                    progressBar.progress = progressStatus
                    progressText.text = "$progressStatus%"

                    // Update loading step based on progress
                    updateLoadingStep(progressStatus)

                    // Update loading text animation
                    animateLoadingText()
                }

                try {
                    // Simulate loading time
                    Thread.sleep(30)
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                }
            }

            // Loading complete, navigate to main activity
            handler.post {
                navigateToMainActivity()
            }
        }).start()
    }

    private fun updateLoadingStep(progress: Int) {
        when (progress) {
            in 0..15 -> stepText.text = loadingSteps[0]
            in 16..30 -> stepText.text = loadingSteps[1]
            in 31..50 -> stepText.text = loadingSteps[2]
            in 51..70 -> stepText.text = loadingSteps[3]
            in 71..85 -> stepText.text = loadingSteps[4]
            else -> stepText.text = loadingSteps[5]
        }
    }

    private fun animateLoadingText() {
        val dots = when (progressStatus % 4) {
            0 -> "."
            1 -> ".."
            2 -> "..."
            else -> "...."
        }
        loadingText.text = "Initializing AI Models$dots"
    }

    private fun navigateToMainActivity() {
        // Check if onboarding is completed and if user is logged in
        val sharedPreferences = getSharedPreferences("app_prefs", MODE_PRIVATE)
        val isOnboardingCompleted = sharedPreferences.getBoolean("onboarding_completed", false)
        val isLoggedIn = sharedPreferences.getBoolean("user_logged_in", false)

        val intent = when {
            !isOnboardingCompleted -> Intent(this, OnboardingActivity::class.java)
            isLoggedIn -> Intent(this, MainActivity::class.java)
            else -> Intent(this,LoginActivity::class.java)
        }

        startActivity(intent)
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        // Clean up handler to prevent memory leaks
        handler.removeCallbacksAndMessages(null)
    }
}
