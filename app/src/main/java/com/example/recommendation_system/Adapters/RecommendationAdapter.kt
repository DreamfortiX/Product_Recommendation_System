package com.example.recommendation_system.Adapters


import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.RatingBar
import android.widget.ProgressBar
import android.widget.TextView

import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.recommendation_system.R
import com.example.recommendation_system.data.model.Product
import com.google.android.material.chip.Chip

class RecommendationAdapter(
    private val onRecommendationClickListener: OnRecommendationClickListener
) : ListAdapter<Product, RecommendationAdapter.RecommendationViewHolder>(RecommendationDiffCallback()) {

    private val selectedAsins = mutableSetOf<String>()

    interface OnRecommendationClickListener {
        fun onRecommendationClick(product: Product)
        fun onViewDetailsClick(product: Product)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecommendationViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_recommendation, parent, false)
        return RecommendationViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecommendationViewHolder, position: Int) {
        val product = getItem(position)
        holder.bind(product)
    }

    fun getSelectedProducts(): List<Product> {
        return currentList.filter { selectedAsins.contains(it.asin) }
    }

    inner class RecommendationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val chipMethod: Chip = itemView.findViewById(R.id.chipMethod)
        private val tvTitle: TextView = itemView.findViewById(R.id.tvTitle)
        private val tvScore: TextView = itemView.findViewById(R.id.tvScore)
        private val tvCategory: TextView = itemView.findViewById(R.id.tvCategory)
        private val tvPrice: TextView = itemView.findViewById(R.id.tvPrice)
        private val tvRating: TextView = itemView.findViewById(R.id.tvRating)
        private val ratingBar: RatingBar = itemView.findViewById(R.id.ratingBar)
        private val progressScore: ProgressBar = itemView.findViewById(R.id.progressScore)
        private val btnViewDetails: Button = itemView.findViewById(R.id.btnViewDetails)
        private val cbSelect: CheckBox = itemView.findViewById(R.id.cbSelect)

        fun bind(product: Product) {

            // Set recommendation method chip
            chipMethod.text = product.method?.replace("_", " ")?.capitalizeWords()
            setMethodChipColor(chipMethod, product.method)

            // Set product title
            tvTitle.text = product.title

            // Set similarity score
            val score = product.score ?: 0.0
            tvScore.text = String.format("%.2f", score)

            // Update progress bar based on score
            val progress = (score * 100).toInt()
            progressScore.progress = progress

            // Set progress bar color based on score
            setProgressBarColor(progressScore, progress)

            // Set category
            tvCategory.text = product.category

            // Set price
            tvPrice.text = "$${String.format("%.2f", product.price)}"

            // Set rating
            tvRating.text = "${product.stars}/5"
            ratingBar.rating = product.stars.toFloat()

            // Configure selection checkbox
            cbSelect.setOnCheckedChangeListener(null)
            cbSelect.isChecked = selectedAsins.contains(product.asin)

            cbSelect.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    selectedAsins.add(product.asin)
                } else {
                    selectedAsins.remove(product.asin)
                }
            }

            // Set click listeners
            itemView.setOnClickListener {
                onRecommendationClickListener.onRecommendationClick(product)
            }

            btnViewDetails.setOnClickListener {
                onRecommendationClickListener.onViewDetailsClick(product)
            }

            // Add ripple effect
            itemView.isClickable = true
        }

        private fun setMethodChipColor(chip: Chip, method: String?) {
            val colorRes = when (method?.lowercase()) {
                "cosine" -> R.color.method_cosine
                "decision_tree" -> R.color.method_decision_tree
                "gnn" -> R.color.method_gnn
                "hybrid" -> R.color.method_hybrid
                else -> R.color.method_default
            }

            chip.chipBackgroundColor = ContextCompat.getColorStateList(itemView.context, colorRes)
        }

        private fun setProgressBarColor(progressBar: ProgressBar, progress: Int) {
            val colorRes = when {
                progress >= 80 -> R.color.score_high
                progress >= 60 -> R.color.score_medium
                else -> R.color.score_low
            }

            val colorInt = ContextCompat.getColor(itemView.context, colorRes)
            progressBar.progressDrawable?.setTint(colorInt)
        }

        private fun String.capitalizeWords(): String =
            this.split(" ").joinToString(" ") { word ->
                word.replaceFirstChar { it.uppercase() }
            }
    }

    class RecommendationDiffCallback : DiffUtil.ItemCallback<Product>() {
        override fun areItemsTheSame(oldItem: Product, newItem: Product): Boolean {
            return oldItem.asin == newItem.asin
        }

        override fun areContentsTheSame(oldItem: Product, newItem: Product): Boolean {
            return oldItem == newItem
        }
    }
}