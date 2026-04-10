package com.caas.app.ui.provider.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.caas.app.data.model.Product
import com.caas.app.databinding.ItemProductSelectableBinding

class ProductSelectableAdapter : ListAdapter<Product, ProductSelectableAdapter.SelectableViewHolder>(DIFF_CALLBACK) {

    private val selectedIds = mutableSetOf<String>()

    fun getSelectedIds(): List<String> = selectedIds.toList()

    fun setAlreadyAssociated(ids: List<String>) {
        selectedIds.clear()
        selectedIds.addAll(ids)
        notifyDataSetChanged()
    }

    inner class SelectableViewHolder(
        private val binding: ItemProductSelectableBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(product: Product) {
            binding.tvProductName.text = product.name
            binding.tvProductSku.text = product.sku
            binding.cbProduct.isChecked = selectedIds.contains(product.id)

            binding.root.setOnClickListener {
                if (selectedIds.contains(product.id)) {
                    selectedIds.remove(product.id)
                } else {
                    selectedIds.add(product.id)
                }
                binding.cbProduct.isChecked = selectedIds.contains(product.id)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SelectableViewHolder {
        val binding = ItemProductSelectableBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return SelectableViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SelectableViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<Product>() {
            override fun areItemsTheSame(oldItem: Product, newItem: Product): Boolean =
                oldItem.id == newItem.id

            override fun areContentsTheSame(oldItem: Product, newItem: Product): Boolean =
                oldItem == newItem
        }
    }
}
