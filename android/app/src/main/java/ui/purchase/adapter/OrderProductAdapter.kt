package com.caas.app.ui.purchase.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.caas.app.data.model.PurchaseOrderItem
import com.caas.app.databinding.ItemOrderProductBinding

class OrderProductAdapter(
    private val onQuantityChanged: (PurchaseOrderItem, Int) -> Unit,
    private val onRemove: (PurchaseOrderItem) -> Unit
) : ListAdapter<PurchaseOrderItem, OrderProductAdapter.ViewHolder>(DiffCallback) {

    inner class ViewHolder(private val binding: ItemOrderProductBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: PurchaseOrderItem) {
            binding.tvProductName.text = item.productName
            binding.etQuantity.setText(item.quantity.toString())
            binding.tvUnitCost.text = if (item.unitCost > 0) "$ %.2f".format(item.unitCost) else "-"

            binding.etQuantity.setOnFocusChangeListener { _, hasFocus ->
                if (!hasFocus) {
                    val qty = binding.etQuantity.text.toString().toIntOrNull() ?: item.quantity
                    if (qty != item.quantity) onQuantityChanged(item, qty)
                }
            }

            binding.btnRemove.setOnClickListener { onRemove(item) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemOrderProductBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    companion object DiffCallback : DiffUtil.ItemCallback<PurchaseOrderItem>() {
        override fun areItemsTheSame(oldItem: PurchaseOrderItem, newItem: PurchaseOrderItem) =
            oldItem.productId == newItem.productId

        override fun areContentsTheSame(oldItem: PurchaseOrderItem, newItem: PurchaseOrderItem) =
            oldItem == newItem
    }
}
