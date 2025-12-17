package com.example.recommendation_system

import android.os.Bundle
import android.widget.Toast
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.recommendation_system.Adapters.CompareProductsAdapter
import com.example.recommendation_system.data.model.Product
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.Description
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.formatter.PercentFormatter
import com.github.mikephil.charting.utils.ColorTemplate

class CompareProductsActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_PRODUCTS = "extra_products"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_compare_products)

        val products = intent.getSerializableExtra(EXTRA_PRODUCTS) as? ArrayList<Product> ?: arrayListOf()

        if (products.isEmpty()) {
            Toast.makeText(this, "No products to compare", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        val tvTitle: TextView = findViewById(R.id.tvCompareTitle)
        val recyclerView: RecyclerView = findViewById(R.id.recyclerCompare)
        val barChart: BarChart = findViewById(R.id.barChartPrices)
        val pieChart: PieChart = findViewById(R.id.pieChartScores)

        tvTitle.text = "Comparing ${products.size} products"

        recyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        recyclerView.adapter = CompareProductsAdapter(products)

        setupBarChart(barChart, products)
        setupPieChart(pieChart, products)
    }

    private fun setupBarChart(barChart: BarChart, products: List<Product>) {
        val entries = products.mapIndexed { index, product ->
            BarEntry(index.toFloat(), product.price.toFloat())
        }

        val dataSet = BarDataSet(entries, "Price").apply {
            colors = ColorTemplate.MATERIAL_COLORS.toList()
            valueTextSize = 10f
        }

        val barData = BarData(dataSet)
        barChart.data = barData

        // X-axis labels with truncated product titles
        val titles = products.map { it.title.take(10) }
        barChart.xAxis.valueFormatter = IndexAxisValueFormatter(titles)
        barChart.xAxis.setDrawGridLines(false)
        barChart.xAxis.granularity = 1f
        barChart.xAxis.labelRotationAngle = -15f
        barChart.axisLeft.setDrawGridLines(true)
        barChart.axisRight.isEnabled = false
        barChart.setFitBars(true)

        // minimal description
        val description = Description().apply { text = "Prices" }
        barChart.description = description
        barChart.invalidate()
    }

    private fun setupPieChart(pieChart: PieChart, products: List<Product>) {
        val totalScore = products.sumOf { it.score ?: 0.0 }

        val entries = if (totalScore > 0) {
            products.map { product ->
                PieEntry((product.score ?: 0.0).toFloat(), product.title.take(10))
            }
        } else {
            products.map { product ->
                PieEntry(1f, product.title.take(10))
            }
        }

        val dataSet = PieDataSet(entries, "Score share").apply {
            colors = ColorTemplate.MATERIAL_COLORS.toList()
            valueTextSize = 10f
            sliceSpace = 2f
        }

        val pieData = PieData(dataSet)
        if (totalScore > 0) {
            pieData.setValueFormatter(PercentFormatter(pieChart))
        }
        pieData.setValueTextSize(10f)
        pieChart.data = pieData

        val description = Description().apply { text = if (totalScore > 0) "Score %" else "Distribution" }
        pieChart.description = description
        pieChart.setUsePercentValues(totalScore > 0)
        pieChart.centerText = "Scores"
        pieChart.setEntryLabelTextSize(10f)
        pieChart.invalidate()
    }
}
