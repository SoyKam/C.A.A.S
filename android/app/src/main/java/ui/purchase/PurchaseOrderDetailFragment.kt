package com.caas.app.ui.purchase

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.caas.app.R
import com.caas.app.core.result.Result
import com.caas.app.data.model.PurchaseOrder
import com.caas.app.data.model.PurchaseOrderStatus
import com.caas.app.databinding.FragmentPurchaseOrderDetailBinding
import com.caas.app.ui.purchase.adapter.OrderDetailProductAdapter
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class PurchaseOrderDetailFragment : Fragment() {

    private var _binding: FragmentPurchaseOrderDetailBinding? = null
    private val binding get() = _binding!!

    private val viewModel: PurchaseOrderViewModel by activityViewModels()
    private val args: PurchaseOrderDetailFragmentArgs by navArgs()
    private lateinit var itemsAdapter: OrderDetailProductAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPurchaseOrderDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupClickListeners()
        observeOrderDetailState()
        observeUpdateStatusState()
        viewModel.loadOrderDetail(args.businessId, args.orderId)
    }

    private fun setupRecyclerView() {
        itemsAdapter = OrderDetailProductAdapter()
        binding.rvItems.layoutManager = LinearLayoutManager(requireContext())
        binding.rvItems.adapter = itemsAdapter
        binding.rvItems.isNestedScrollingEnabled = false
    }

    private fun setupClickListeners() {
        binding.btnBack.setOnClickListener { findNavController().navigateUp() }

        binding.btnMarkSent.setOnClickListener {
            viewModel.updateOrderStatus(args.businessId, args.orderId, PurchaseOrderStatus.SENT)
        }

        binding.btnMarkReceived.setOnClickListener {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("Confirmar recepción")
                .setMessage("¿Confirmar recepción? Esto actualizará el stock automáticamente.")
                .setPositiveButton("Confirmar") { _, _ ->
                    viewModel.receiveOrder(args.businessId, args.orderId)
                }
                .setNegativeButton("Cancelar", null)
                .show()
        }

        binding.btnCancel.setOnClickListener {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("Cancelar orden")
                .setMessage("¿Cancelar esta orden de compra? Esta acción no se puede deshacer.")
                .setPositiveButton("Cancelar orden") { _, _ ->
                    viewModel.updateOrderStatus(args.businessId, args.orderId, PurchaseOrderStatus.CANCELLED)
                }
                .setNegativeButton("Volver", null)
                .show()
        }
    }

    private fun observeOrderDetailState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.orderDetailState.collect { state ->
                    when (state) {
                        is Result.Loading -> showLoading(true)
                        is Result.Success -> {
                            showLoading(false)
                            displayOrder(state.data)
                        }
                        is Result.Error -> {
                            showLoading(false)
                            Snackbar.make(binding.root, state.message, Snackbar.LENGTH_LONG).show()
                        }
                        null -> showLoading(false)
                    }
                }
            }
        }
    }

    private fun observeUpdateStatusState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.updateStatusState.collect { state ->
                    when (state) {
                        is Result.Loading -> showLoading(true)
                        is Result.Success -> {
                            showLoading(false)
                            viewModel.resetUpdateState()
                            displayOrder(state.data)
                            Snackbar.make(binding.root, "Estado actualizado", Snackbar.LENGTH_SHORT).show()
                        }
                        is Result.Error -> {
                            showLoading(false)
                            Snackbar.make(binding.root, state.message, Snackbar.LENGTH_LONG).show()
                            viewModel.resetUpdateState()
                        }
                        null -> {}
                    }
                }
            }
        }
    }

    private fun displayOrder(order: PurchaseOrder) {
        binding.tvProviderName.text = order.providerName

        val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        binding.tvDate.text = sdf.format(Date(order.createdAt))

        binding.tvAutoGenerated.visibility =
            if (order.isAutoGenerated) View.VISIBLE else View.GONE

        if (order.notes.isNotBlank()) {
            binding.tvNotes.text = order.notes
            binding.tvNotes.visibility = View.VISIBLE
        } else {
            binding.tvNotes.visibility = View.GONE
        }

        val (statusText, statusColor) = when (order.status) {
            PurchaseOrderStatus.PENDING -> "Pendiente" to R.color.yellow
            PurchaseOrderStatus.SENT -> "Enviada" to R.color.blue
            PurchaseOrderStatus.RECEIVED -> "Recibida" to R.color.green
            PurchaseOrderStatus.CANCELLED -> "Cancelada" to R.color.red
        }
        binding.chipStatus.text = statusText
        binding.chipStatus.setChipBackgroundColorResource(statusColor)

        itemsAdapter.submitList(order.items)

        // Mostrar botones según estado actual
        binding.btnMarkSent.visibility = View.GONE
        binding.btnMarkReceived.visibility = View.GONE
        binding.btnCancel.visibility = View.GONE

        when (order.status) {
            PurchaseOrderStatus.PENDING -> {
                binding.btnMarkSent.visibility = View.VISIBLE
                binding.btnCancel.visibility = View.VISIBLE
            }
            PurchaseOrderStatus.SENT -> {
                binding.btnMarkReceived.visibility = View.VISIBLE
                binding.btnCancel.visibility = View.VISIBLE
            }
            PurchaseOrderStatus.RECEIVED, PurchaseOrderStatus.CANCELLED -> { /* sin acciones */ }
        }
    }

    private fun showLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
