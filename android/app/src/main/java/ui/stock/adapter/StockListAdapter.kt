package com.caas.app.ui.stock.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.caas.app.data.model.Stock
import com.caas.app.databinding.ItemStockBinding

class StockListAdapter(
    private val onViewMovementsClick: (Stock) -> Unit
) : ListAdapter<Stock, StockListAdapter.StockViewHolder>(DIFF_CALLBACK) {

    inner class StockViewHolder(
        private val binding: ItemStockBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(stock: Stock) {
            binding.tvProductName.text = stock.productName
            binding.tvQuantity.text = stock.quantity.toString()

            val isCritical = stock.quantity <= stock.minStock
            val statusText = if (isCritical) "Crítico" else "Normal"
            val statusColor = if (isCritical) 
                binding.root.context.getColor(android.R.color.holo_red_light)
            else
                binding.root.context.getColor(android.R.color.holo_green_light)
            
            binding.tvStatus.text = statusText
            binding.tvStatus.setTextColor(statusColor)

            binding.root.setOnClickListener {
                onViewMovementsClick(stock)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StockViewHolder {
        val binding = ItemStockBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return StockViewHolder(binding)
    }

    override fun onBindViewHolder(holder: StockViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<Stock>() {
            override fun areItemsTheSame(oldItem: Stock, newItem: Stock): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: Stock, newItem: Stock): Boolean {
                return oldItem == newItem
            }
        }
    }
}
