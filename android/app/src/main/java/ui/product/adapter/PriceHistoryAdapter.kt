package com.caas.app.ui.product.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.caas.app.R
import com.caas.app.data.model.PriceHistory
import com.caas.app.databinding.ItemPriceHistoryBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class PriceHistoryAdapter : ListAdapter<PriceHistory, PriceHistoryAdapter.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemPriceHistoryBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ViewHolder(private val binding: ItemPriceHistoryBinding) :
        RecyclerView.ViewHolder(binding.root) {

        private val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())

        fun bind(item: PriceHistory) {
            binding.tvChangedAt.text = dateFormat.format(Date(item.changedAt))
            binding.tvChangedBy.text = "Modificado por: ${item.changedByEmail.ifBlank { item.changedBy }}"

            binding.tvPreviousCost.text = formatPrice(item.previousCostPrice)
            binding.tvNewCost.text = formatPrice(item.newCostPrice)
            binding.tvNewCost.setTextColor(
                ContextCompat.getColor(
                    binding.root.context,
                    if (item.newCostPrice > item.previousCostPrice) R.color.red else R.color.green
                )
            )

            binding.tvPreviousSale.text = formatPrice(item.previousSalePrice)
            binding.tvNewSale.text = formatPrice(item.newSalePrice)
            binding.tvNewSale.setTextColor(
                ContextCompat.getColor(
                    binding.root.context,
                    if (item.newSalePrice > item.previousSalePrice) R.color.red else R.color.green
                )
            )
        }

        private fun formatPrice(value: Double): String = String.format("$%.2f", value)
    }

    class DiffCallback : DiffUtil.ItemCallback<PriceHistory>() {
        override fun areItemsTheSame(oldItem: PriceHistory, newItem: PriceHistory) =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: PriceHistory, newItem: PriceHistory) =
            oldItem == newItem
    }
}
