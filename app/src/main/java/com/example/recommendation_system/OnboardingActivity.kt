package com.example.recommendation_system

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.BounceInterpolator
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.example.choice.Adapters.OnboardingAdapter
import com.example.choice.data.model.OnboardingItem
import kotlin.apply
import kotlin.collections.forEachIndexed
import kotlin.jvm.java
import androidx.core.content.edit

class OnboardingActivity : AppCompatActivity() {

    private lateinit var viewPager: ViewPager2
    private lateinit var btnNext: CardView
    private lateinit var textNext: TextView
    private lateinit var textSkip: TextView
    private lateinit var btnSkip: CardView
    private lateinit var indicator1: View
    private lateinit var indicator2: View
    private lateinit var indicator3: View

    private val onboardingItems = listOf(
        OnboardingItem(
            title = "Cosine Similarity",
            description = "Content-based matching using text similarity algorithms to find products with similar descriptions, titles, and features.",
            lottieAnimation = R.raw.animation_cosine,
            activeCard = 0
        ),
        OnboardingItem(
            title = "Decision Tree",
            description = "Feature-based recommendations using machine learning to analyze product attributes and user preferences for smart suggestions.",
            lottieAnimation = R.raw.animation_decision_tree,
            activeCard = 1
        ),
        OnboardingItem(
            title = "Graph Neural Network",
            description = "Advanced graph-based intelligence that understands product relationships and connections for highly accurate recommendations.",
            lottieAnimation = R.raw.animation_gnn,
            activeCard = 2
        )
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_onboarding)

        hideStatusBar()

        initViews()
        setupViewPager()
        setupListeners()
        setupAnimations()
    }

    private fun initViews() {
        viewPager = findViewById(R.id.viewPager)
        btnNext = findViewById(R.id.btnNext)
        textNext = findViewById(R.id.textNext)
        textSkip = findViewById(R.id.textSkip)
        indicator1 = findViewById(R.id.indicator1)
        indicator2 = findViewById(R.id.indicator2)
        indicator3 = findViewById(R.id.indicator3)
        btnSkip = findViewById(R.id.btnSkip)
    }

    private fun setupViewPager() {
        val adapter = OnboardingAdapter(onboardingItems) { cardIndex ->
            // Navigate to the page of the clicked card
            viewPager.currentItem = cardIndex
        }

        viewPager.adapter = adapter

        // Add page change callback
        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                updateUI(position)
                animatePageTransition(position)
            }
        })
    }

    private fun updateUI(position: Int) {
        // Update indicators
        updateIndicators(position)

        // Update next button text
        textNext.text = if (position == onboardingItems.size - 1) "Get Started" else "Next"

        // Update skip button visibility - only show on first screen
        btnSkip.visibility = if (position == 0) View.VISIBLE else View.GONE
    }

    private fun updateIndicators(position: Int) {
        val indicators: List<View> = listOf(indicator1, indicator2, indicator3)

        indicators.forEachIndexed { index, indicator ->
            if (index == position) {
                // Active indicator
                indicator.animate()
                    .scaleX(3f)
                    .scaleY(1f)
                    .setDuration(300)
                    .start()
                indicator.setBackgroundResource(R.drawable.indicator_active)
            } else {
                // Inactive indicator
                indicator.animate()
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(300)
                    .start()
                indicator.setBackgroundResource(R.drawable.indicator_inactive)
            }
        }
    }

    private fun setupListeners() {
        // Skip button
        textSkip.setOnClickListener {
            navigateToAuthActivity()
        }

        // Next button
        btnNext.setOnClickListener {
            if (viewPager.currentItem < onboardingItems.size - 1) {
                viewPager.currentItem = viewPager.currentItem + 1
            } else {
                navigateToAuthActivity()
            }
        }
    }

    private fun setupAnimations() {
        // Initial entrance animation
        val initialAnimator = AnimatorSet()
        val slideUp = ObjectAnimator.ofFloat(viewPager, "translationY", 100f, 0f)
        val fadeIn = ObjectAnimator.ofFloat(viewPager, "alpha", 0f, 1f)

        initialAnimator.playTogether(slideUp, fadeIn)
        initialAnimator.duration = 500
        initialAnimator.interpolator = AccelerateDecelerateInterpolator()
        initialAnimator.start()

        // Button pulse animation
        startButtonPulseAnimation()
    }

    private fun startButtonPulseAnimation() {
        val pulseAnimator = ObjectAnimator.ofFloat(btnNext, "scaleX", 1f, 1.05f, 1f)
        pulseAnimator.duration = 1000
        pulseAnimator.repeatCount = ObjectAnimator.INFINITE
        pulseAnimator.repeatMode = ObjectAnimator.REVERSE
        pulseAnimator.start()
    }

    private fun animatePageTransition(position: Int) {
        // Get current view holder
        val recyclerView = viewPager.getChildAt(0) as? RecyclerView
        val viewHolder = recyclerView?.findViewHolderForAdapterPosition(position)

        if (viewHolder is OnboardingAdapter.ViewHolder) {
            // Reset initial state for a clean animation
            viewHolder.textTitle.alpha = 0f
            viewHolder.textTitle.translationY = 40f

            viewHolder.textDescription.alpha = 0f
            viewHolder.textDescription.translationY = 40f

            viewHolder.lottieAnimation.alpha = 0f
            viewHolder.lottieAnimation.scaleX = 0.9f
            viewHolder.lottieAnimation.scaleY = 0.9f

            // Animate title
            viewHolder.textTitle.animate()
                .translationY(0f)
                .alpha(1f)
                .setDuration(300)
                .setInterpolator(AccelerateDecelerateInterpolator())
                .start()

            // Animate description slightly after title
            viewHolder.textDescription.animate()
                .translationY(0f)
                .alpha(1f)
                .setDuration(300)
                .setStartDelay(80)
                .setInterpolator(AccelerateDecelerateInterpolator())
                .start()

            // Animate Lottie animation
            viewHolder.lottieAnimation.animate()
                .alpha(1f)
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(400)
                .setInterpolator(BounceInterpolator())
                .start()
        }
    }

    private fun navigateToAuthActivity() {
        // Mark onboarding as completed
        val sharedPreferences = getSharedPreferences("app_prefs", MODE_PRIVATE)
        sharedPreferences.edit { putBoolean("onboarding_completed", true) }

        // Clear the back stack so user can't go back to onboarding
        val intent = Intent(this, LoginActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
        finish()
    }

    private fun hideStatusBar() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        val controller = WindowInsetsControllerCompat(window, window.decorView)
        controller.hide(WindowInsetsCompat.Type.statusBars())
        controller.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
    }
}