package com.caas.app.ui.inventory.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.caas.app.databinding.ItemInventoryProductBinding
import com.caas.app.domain.model.InventorySummaryItem

class InventorySummaryAdapter :
    ListAdapter<InventorySummaryItem, InventorySummaryAdapter.ViewHolder>(DIFF_CALLBACK) {

    inner class ViewHolder(
        private val binding: ItemInventoryProductBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: InventorySummaryItem) {
            binding.tvProductName.text = item.productName
            binding.tvSku.text = if (item.sku.isNotBlank()) "SKU: ${item.sku}" else "Sin SKU"
            binding.tvQuantity.text = item.quantity.toString()
            binding.tvMinStock.text = "Mínimo: ${item.minStock}"

            if (item.isCritical) {
                binding.tvStockStatus.text = "CRITICO"
                binding.tvStockStatus.setBackgroundColor(Color.parseColor("#B00020"))
                binding.tvQuantity.setTextColor(Color.parseColor("#B00020"))
            } else {
                binding.tvStockStatus.text = "OK"
                binding.tvStockStatus.setBackgroundColor(Color.parseColor("#4CAF50"))
                binding.tvQuantity.setTextColor(Color.parseColor("#FF8000"))
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemInventoryProductBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<InventorySummaryItem>() {
            override fun areItemsTheSame(old: InventorySummaryItem, new: InventorySummaryItem) =
                old.stockId == new.stockId

            override fun areContentsTheSame(old: InventorySummaryItem, new: InventorySummaryItem) =
                old == new
        }
    }
}
