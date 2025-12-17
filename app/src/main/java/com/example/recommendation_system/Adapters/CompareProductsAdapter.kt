package com.example.recommendation_system.Adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.recommendation_system.R
import com.example.recommendation_system.data.model.Product

class CompareProductsAdapter(
    private val products: List<Product>
) : RecyclerView.Adapter<CompareProductsAdapter.CompareViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CompareViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_compare_product, parent, false)
        return CompareViewHolder(view)
    }

    override fun onBindViewHolder(holder: CompareViewHolder, position: Int) {
        holder.bind(products[position])
    }

    override fun getItemCount(): Int = products.size

    class CompareViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvTitle: TextView = itemView.findViewById(R.id.tvCompareProductTitle)
        private val tvCategory: TextView = itemView.findViewById(R.id.tvCompareCategory)
        private val tvPrice: TextView = itemView.findViewById(R.id.tvComparePrice)
        private val tvRating: TextView = itemView.findViewById(R.id.tvCompareRating)
        private val tvScore: TextView = itemView.findViewById(R.id.tvCompareScore)
        private val tvMethod: TextView = itemView.findViewById(R.id.tvCompareMethod)

        fun bind(product: Product) {
            tvTitle.text = product.title
            tvCategory.text = product.category
            tvPrice.text = "$${String.format("%.2f", product.price)}"
            tvRating.text = "${product.stars}/5.0"
            tvScore.text = product.score?.let { String.format("%.2f", it) } ?: "-"
            tvMethod.text = product.method ?: "-"
        }
    }
}
