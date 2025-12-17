package com.example.recommendation_system.Adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.recommendation_system.R
import android.widget.Button
import android.widget.RatingBar
import com.example.recommendation_system.data.model.Product


class ProductAdapter(
    private val onProductClickListener: OnProductClickListener
) : ListAdapter<Product, ProductAdapter.ProductViewHolder>(ProductDiffCallback()) {

    interface OnProductClickListener {
        fun onProductClick(product: Product)
        fun onGetRecommendationsClick(product: Product)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_product, parent, false)
        return ProductViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        val product = getItem(position)
        holder.bind(product)
    }

    inner class ProductViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvTitle: TextView = itemView.findViewById(R.id.tvTitle)
        private val tvCategory: TextView = itemView.findViewById(R.id.tvCategory)
        private val tvPrice: TextView = itemView.findViewById(R.id.tvPrice)
        private val tvRating: TextView = itemView.findViewById(R.id.tvRating)
        private val tvReviews: TextView = itemView.findViewById(R.id.tvReviews)
        private val ratingBar: RatingBar = itemView.findViewById(R.id.ratingBar)
        private val btnGetRecommendations: Button = itemView.findViewById(R.id.btnGetRecommendations)

        fun bind(product: Product) {
            // Set product title
            tvTitle.text = product.title

            // Set category
            tvCategory.text = product.category

            // Set price
            tvPrice.text = "$${String.format("%.2f", product.price)}"

            // Set rating
            tvRating.text = product.stars.toString()
            ratingBar.rating = product.stars.toFloat()

            // Set reviews (simulated - you might want to add this to your Product model)
            val reviewsText = if (product.description?.contains("Reviews") == true) {
                product.description
            } else {
                "${(product.stars * 100).toInt()} reviews"
            }
            tvReviews.text = reviewsText

            // Set click listeners
            itemView.setOnClickListener {
                onProductClickListener.onProductClick(product)
            }

            btnGetRecommendations.setOnClickListener {
                onProductClickListener.onGetRecommendationsClick(product)
            }

            // Add ripple effect
            itemView.isClickable = true
        }
    }

    class ProductDiffCallback : DiffUtil.ItemCallback<Product>() {
        override fun areItemsTheSame(oldItem: Product, newItem: Product): Boolean {
            return oldItem.asin == newItem.asin
        }

        override fun areContentsTheSame(oldItem: Product, newItem: Product): Boolean {
            return oldItem == newItem
        }
    }
}