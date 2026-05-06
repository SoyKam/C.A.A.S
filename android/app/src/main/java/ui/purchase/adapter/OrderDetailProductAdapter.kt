package com.caas.app.ui.purchase.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.caas.app.data.model.PurchaseOrderItem
import com.caas.app.databinding.ItemOrderDetailProductBinding

class OrderDetailProductAdapter :
    ListAdapter<PurchaseOrderItem, OrderDetailProductAdapter.ViewHolder>(DiffCallback) {

    inner class ViewHolder(private val binding: ItemOrderDetailProductBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: PurchaseOrderItem) {
            binding.tvProductName.text = item.productName
            binding.tvQuantity.text = "${item.quantity} unid."
            binding.tvUnitCost.text = if (item.unitCost > 0) "$ %.2f".format(item.unitCost) else "-"
            val subtotal = item.quantity * item.unitCost
            binding.tvSubtotal.text = if (subtotal > 0) "$ %.2f".format(subtotal) else "-"
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemOrderDetailProductBinding.inflate(
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
