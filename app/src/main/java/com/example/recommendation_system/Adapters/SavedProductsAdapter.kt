package com.example.recommendation_system.Adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.recommendation_system.R
import com.example.recommendation_system.data.local.SavedProduct

class SavedProductsAdapter : RecyclerView.Adapter<SavedProductsAdapter.SavedVH>() {

    private val items = mutableListOf<SavedProduct>()

    fun submitList(data: List<SavedProduct>) {
        items.clear()
        items.addAll(data)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SavedVH {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_saved_product, parent, false)
        return SavedVH(v)
    }

    override fun onBindViewHolder(holder: SavedVH, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    class SavedVH(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvTitle: TextView = itemView.findViewById(R.id.tvTitle)
        private val tvCategory: TextView = itemView.findViewById(R.id.tvCategory)
        private val tvPrice: TextView = itemView.findViewById(R.id.tvPrice)
        private val tvBadge: TextView = itemView.findViewById(R.id.tvBadge)

        fun bind(item: SavedProduct) {
            tvTitle.text = item.title
            tvCategory.text = item.category
            tvPrice.text = "$" + String.format("%.2f", item.price)
            tvBadge.visibility = if (item.isLowPrice) View.VISIBLE else View.GONE
        }
    }
}
