package com.caas.app.ui.stock

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.caas.app.data.model.StockMovement
import com.caas.app.databinding.FragmentMovementDetailBinding
import com.caas.app.ui.stock.adapter.movementVisuals
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MovementDetailFragment : Fragment() {

    private var _binding: FragmentMovementDetailBinding? = null
    private val binding get() = _binding!!

    private val viewModel: StockViewModel by activityViewModels()
    private val args: MovementDetailFragmentArgs by navArgs()

    private val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMovementDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.btnBack.setOnClickListener { findNavController().navigateUp() }
        observeSelectedMovement()
    }

    private fun observeSelectedMovement() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.selectedMovement.collect { movement ->
                    if (movement != null && movement.id == args.movementId) {
                        bindMovement(movement)
                    }
                }
            }
        }
    }

    private fun bindMovement(movement: StockMovement) {
        val ctx = binding.root.context

        binding.tvProductName.text = movement.productName
        binding.tvQuantity.text = movement.quantity.toString()
        binding.tvDate.text = dateFormat.format(Date(movement.createdAt))
        binding.tvReason.text = movement.reason.ifBlank { "—" }
        binding.tvCreatedBy.text = movement.createdBy.ifBlank { "—" }

        val visuals = movementVisuals(movement.type)
        binding.tvMovementType.text = visuals.typeLabel
        binding.iconContainer.background = ContextCompat.getDrawable(ctx, visuals.iconBgRes)
        binding.ivMovementIcon.setImageResource(visuals.iconRes)
        binding.ivMovementIcon.imageTintList = ColorStateList.valueOf(
            ContextCompat.getColor(ctx, visuals.colorRes)
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
