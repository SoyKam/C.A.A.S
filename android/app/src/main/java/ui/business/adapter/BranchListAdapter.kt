package com.caas.app.ui.business.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.caas.app.data.model.Branch
import com.caas.app.databinding.ItemBranchBinding

class BranchListAdapter(
    private val onBranchClick: (branchId: String) -> Unit
) : ListAdapter<Branch, BranchListAdapter.BranchViewHolder>(DIFF_CALLBACK) {

    inner class BranchViewHolder(
        private val binding: ItemBranchBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(branch: Branch) {
            binding.tvBranchName.text = branch.name
            binding.tvBranchAddress.text = branch.address

            binding.btnViewBranch.setOnClickListener {
                onBranchClick(branch.id)
            }

            binding.root.setOnClickListener {
                onBranchClick(branch.id)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BranchViewHolder {
        val binding = ItemBranchBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return BranchViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BranchViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<Branch>() {
            override fun areItemsTheSame(oldItem: Branch, newItem: Branch): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: Branch, newItem: Branch): Boolean {
                return oldItem == newItem
            }
        }
    }
}
