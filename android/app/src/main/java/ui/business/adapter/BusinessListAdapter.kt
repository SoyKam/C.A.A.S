package com.caas.app.ui.business.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.caas.app.data.model.Business
import com.caas.app.databinding.ItemBusinessBinding

/**
 * Adaptador para mostrar la lista de negocios en RecyclerView.
 * Implementa RF-05 y RF-06: mostrar negocios del usuario.
 */
class BusinessListAdapter(
    private val onBusinessClick: (businessId: String) -> Unit
) : ListAdapter<Business, BusinessListAdapter.BusinessViewHolder>(DIFF_CALLBACK) {

    /**
     * ViewHolder para cada item de negocio.
     */
    inner class BusinessViewHolder(
        private val binding: ItemBusinessBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(business: Business) {
            binding.tvBusinessName.text = business.name
            binding.tvBusinessSector.text = business.sector

            binding.btnViewDetails.setOnClickListener {
                onBusinessClick(business.id)
            }

            // Permitir click en todo el card
            binding.root.setOnClickListener {
                onBusinessClick(business.id)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BusinessViewHolder {
        val binding = ItemBusinessBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return BusinessViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BusinessViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<Business>() {
            override fun areItemsTheSame(oldItem: Business, newItem: Business): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: Business, newItem: Business): Boolean {
                return oldItem == newItem
            }
        }
    }
}


