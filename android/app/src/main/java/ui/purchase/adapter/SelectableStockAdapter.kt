package com.caas.app.ui.purchase.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.caas.app.R
import com.caas.app.data.model.Stock
import com.caas.app.databinding.ItemStockBinding

class SelectableStockAdapter(
    private val onSelect: (Stock) -> Unit
) : ListAdapter<Stock, SelectableStockAdapter.ViewHolder>(DiffCallback) {

    private var selectedId: String? = null

    inner class ViewHolder(private val binding: ItemStockBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(stock: Stock) {
            binding.tvProductName.text = stock.productName
            binding.tvQuantity.text = stock.quantity.toString()
            binding.tvMinStock.text = "Mín: ${stock.minStock}"
            binding.tvStatus.text = ""
            binding.progressStock.visibility = android.view.View.GONE

            val isSelected = stock.productId == selectedId
            binding.root.setCardBackgroundColor(
                ContextCompat.getColor(
                    binding.root.context,
                    if (isSelected) R.color.orange_light else R.color.surface
                )
            )

            binding.root.setOnClickListener {
                val previous = selectedId
                selectedId = stock.productId
                onSelect(stock)
                val prevPos = currentList.indexOfFirst { it.productId == previous }
                val newPos = currentList.indexOfFirst { it.productId == stock.productId }
                if (prevPos >= 0) notifyItemChanged(prevPos)
                if (newPos >= 0) notifyItemChanged(newPos)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemStockBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    companion object DiffCallback : DiffUtil.ItemCallback<Stock>() {
        override fun areItemsTheSame(oldItem: Stock, newItem: Stock) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Stock, newItem: Stock) = oldItem == newItem
    }
}
