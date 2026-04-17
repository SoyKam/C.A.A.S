package com.caas.app.ui.provider.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.caas.app.data.model.Provider
import com.caas.app.databinding.ItemProviderBinding

class ProviderListAdapter(
    private val onItemClick: (Provider) -> Unit
) : ListAdapter<Provider, ProviderListAdapter.ProviderViewHolder>(DIFF_CALLBACK) {

    inner class ProviderViewHolder(
        private val binding: ItemProviderBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(provider: Provider) {
            binding.tvProviderName.text = provider.name
            binding.tvPhone.text = provider.phone
            binding.root.setOnClickListener { onItemClick(provider) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProviderViewHolder {
        val binding = ItemProviderBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ProviderViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ProviderViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<Provider>() {
            override fun areItemsTheSame(oldItem: Provider, newItem: Provider): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: Provider, newItem: Provider): Boolean {
                return oldItem == newItem
            }
        }
    }
}
