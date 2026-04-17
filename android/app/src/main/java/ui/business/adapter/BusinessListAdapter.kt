package com.caas.app.ui.business.adapter

import android.content.res.ColorStateList
import android.graphics.Color
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
            binding.tvBusinessInitial.text = business.name.firstOrNull()?.uppercaseChar()?.toString() ?: "N"

            val (statusLabel, statusColor) = if (business.isActive)
                "Activo" to Color.parseColor("#4CAF50")
            else
                "Inactivo" to Color.parseColor("#9E9E9E")
            binding.tvBusinessStatus.text = statusLabel
            binding.tvBusinessStatus.backgroundTintList = ColorStateList.valueOf(statusColor)

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


