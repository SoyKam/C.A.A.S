package com.caas.app.ui.provider.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.caas.app.databinding.ItemProviderProductBinding

class ProviderProductAdapter(
    private val onRemoveClick: (productId: String) -> Unit
) : ListAdapter<Pair<String, String>, ProviderProductAdapter.ProductChipViewHolder>(DIFF_CALLBACK) {

    inner class ProductChipViewHolder(
        private val binding: ItemProviderProductBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: Pair<String, String>) {
            val (productId, productName) = item
            binding.tvProductName.text = productName
            binding.btnRemove.setOnClickListener { onRemoveClick(productId) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductChipViewHolder {
        val binding = ItemProviderProductBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ProductChipViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ProductChipViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<Pair<String, String>>() {
            override fun areItemsTheSame(
                oldItem: Pair<String, String>,
                newItem: Pair<String, String>
            ): Boolean = oldItem.first == newItem.first

            override fun areContentsTheSame(
                oldItem: Pair<String, String>,
                newItem: Pair<String, String>
            ): Boolean = oldItem == newItem
        }
    }
}
