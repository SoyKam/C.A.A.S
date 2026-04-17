package com.caas.app.ui.stock.adapter

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.caas.app.R
import com.caas.app.data.model.MovementType
import com.caas.app.data.model.StockMovement
import com.caas.app.databinding.ItemStockMovementBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class StockMovementsAdapter(
    private val onItemClick: (StockMovement) -> Unit
) : ListAdapter<StockMovement, StockMovementsAdapter.ViewHolder>(DiffCallback()) {

    private val dateFormat = SimpleDateFormat("dd/MM/yy HH:mm", Locale.getDefault())

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemStockMovementBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(private val binding: ItemStockMovementBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(movement: StockMovement) {
            val ctx = binding.root.context

            binding.tvProductName.text = movement.productName
            binding.tvQuantity.text = movement.quantity.toString()
            binding.tvDate.text = dateFormat.format(Date(movement.createdAt))

            if (movement.reason.isNotBlank()) {
                binding.tvReason.text = movement.reason
                binding.tvReason.visibility = View.VISIBLE
            } else {
                binding.tvReason.visibility = View.GONE
            }

            val (colorRes, iconBgRes, iconRes, typeLabel) = movementVisuals(movement.type)
            binding.tvMovementType.text = typeLabel
            binding.viewAccent.setBackgroundColor(ContextCompat.getColor(ctx, colorRes))
            binding.iconContainer.background = ContextCompat.getDrawable(ctx, iconBgRes)
            binding.ivMovementIcon.setImageResource(iconRes)
            binding.ivMovementIcon.imageTintList = ColorStateList.valueOf(
                ContextCompat.getColor(ctx, colorRes)
            )

            binding.root.setOnClickListener { onItemClick(movement) }
        }
    }

    private class DiffCallback : DiffUtil.ItemCallback<StockMovement>() {
        override fun areItemsTheSame(old: StockMovement, new: StockMovement) = old.id == new.id
        override fun areContentsTheSame(old: StockMovement, new: StockMovement) = old == new
    }
}

internal data class MovementVisuals(
    val colorRes: Int,
    val iconBgRes: Int,
    val iconRes: Int,
    val typeLabel: String
)

internal fun movementVisuals(type: MovementType): MovementVisuals = when (type) {
    MovementType.ENTRY -> MovementVisuals(R.color.green, R.drawable.icon_bg_green, R.drawable.ic_arrow_up, "Entrada")
    MovementType.SALE -> MovementVisuals(R.color.blue, R.drawable.icon_bg_blue, R.drawable.ic_arrow_down, "Venta")
    MovementType.DAMAGE -> MovementVisuals(R.color.red, R.drawable.icon_bg_red, R.drawable.ic_alert, "Daño")
    MovementType.TRANSFER -> MovementVisuals(R.color.orange, R.drawable.icon_bg_orange, R.drawable.ic_transfer, "Traslado")
}
