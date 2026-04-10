package com.caas.app.ui.stock.adapter

import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.caas.app.R
import com.caas.app.data.model.StockAlert
import com.caas.app.databinding.ItemStockAlertGroupedBinding

class LowStockSummaryAdapter(
    private val onMarkReadClick: (StockAlert) -> Unit
) : ListAdapter<LowStockSummaryAdapter.LowStockListItem, RecyclerView.ViewHolder>(DIFF_CALLBACK) {

    sealed class LowStockListItem {
        data class BranchHeader(val branchName: String, val count: Int) : LowStockListItem()
        data class AlertItem(val alert: StockAlert) : LowStockListItem()
    }

    inner class HeaderViewHolder(private val textView: TextView) : RecyclerView.ViewHolder(textView) {
        fun bind(header: LowStockListItem.BranchHeader) {
            textView.text = "${header.branchName}  •  ${header.count} producto(s)"
        }
    }

    inner class AlertViewHolder(
        private val binding: ItemStockAlertGroupedBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: LowStockListItem.AlertItem) {
            binding.tvAlertProductName.text = item.alert.productName
            binding.tvAlertBranchName.text = "Sucursal: ${item.alert.branchName}"
            binding.tvAlertStockInfo.text =
                "Stock actual: ${item.alert.currentStock} | Mínimo: ${item.alert.minStock}"
            binding.btnMarkRead.setOnClickListener { onMarkReadClick(item.alert) }
        }
    }

    override fun getItemViewType(position: Int): Int = when (getItem(position)) {
        is LowStockListItem.BranchHeader -> VIEW_TYPE_HEADER
        is LowStockListItem.AlertItem -> VIEW_TYPE_ITEM
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_HEADER -> {
                val dp = parent.context.resources.displayMetrics.density
                val tv = TextView(parent.context).apply {
                    layoutParams = RecyclerView.LayoutParams(
                        RecyclerView.LayoutParams.MATCH_PARENT,
                        RecyclerView.LayoutParams.WRAP_CONTENT
                    )
                    val hPad = (16 * dp).toInt()
                    val vPad = (8 * dp).toInt()
                    setPadding(hPad, vPad, hPad, vPad)
                    textSize = 13f
                    setTypeface(null, Typeface.BOLD)
                    setTextColor(ContextCompat.getColor(context, R.color.button_orange))
                    setBackgroundColor(ContextCompat.getColor(context, R.color.surface))
                }
                HeaderViewHolder(tv)
            }
            else -> AlertViewHolder(
                ItemStockAlertGroupedBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                )
            )
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = getItem(position)) {
            is LowStockListItem.BranchHeader -> (holder as HeaderViewHolder).bind(item)
            is LowStockListItem.AlertItem -> (holder as AlertViewHolder).bind(item)
        }
    }

    companion object {
        private const val VIEW_TYPE_HEADER = 0
        private const val VIEW_TYPE_ITEM = 1

        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<LowStockListItem>() {
            override fun areItemsTheSame(
                oldItem: LowStockListItem,
                newItem: LowStockListItem
            ): Boolean = when {
                oldItem is LowStockListItem.BranchHeader && newItem is LowStockListItem.BranchHeader ->
                    oldItem.branchName == newItem.branchName
                oldItem is LowStockListItem.AlertItem && newItem is LowStockListItem.AlertItem ->
                    oldItem.alert.id == newItem.alert.id
                else -> false
            }

            override fun areContentsTheSame(
                oldItem: LowStockListItem,
                newItem: LowStockListItem
            ): Boolean = oldItem == newItem
        }
    }
}
