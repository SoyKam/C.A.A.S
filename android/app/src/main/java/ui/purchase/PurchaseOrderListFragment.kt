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
import com.caas.app.data.model.PurchaseOrderStatus
import com.caas.app.databinding.FragmentPurchaseOrderListBinding
import com.caas.app.ui.purchase.adapter.PurchaseOrderAdapter
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch

class PurchaseOrderListFragment : Fragment() {

    private var _binding: FragmentPurchaseOrderListBinding? = null
    private val binding get() = _binding!!

    private val viewModel: PurchaseOrderViewModel by activityViewModels()
    private val args: PurchaseOrderListFragmentArgs by navArgs()
    private lateinit var adapter: PurchaseOrderAdapter

    private var activeStatusFilter: PurchaseOrderStatus? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPurchaseOrderListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupClickListeners()
        setupFilters()
        observeOrderListState()
        observeAutoGenerateState()
    }

    override fun onStart() {
        super.onStart()
        loadOrders()
    }

    private fun setupRecyclerView() {
        adapter = PurchaseOrderAdapter { order ->
            if (findNavController().currentDestination?.id == R.id.purchaseOrderListFragment) {
                findNavController().navigate(
                    PurchaseOrderListFragmentDirections.actionPurchaseOrderListToOrderDetail(
                        args.businessId, order.id
                    )
                )
            }
        }
        binding.rvOrders.layoutManager = LinearLayoutManager(requireContext())
        binding.rvOrders.adapter = adapter
    }

    private fun setupClickListeners() {
        binding.btnBack.setOnClickListener { findNavController().navigateUp() }

        binding.fabAddOrder.setOnClickListener {
            if (findNavController().currentDestination?.id == R.id.purchaseOrderListFragment) {
                findNavController().navigate(
                    PurchaseOrderListFragmentDirections.actionPurchaseOrderListToCreateOrder(
                        args.businessId, args.branchId
                    )
                )
            }
        }

        binding.fabAutoGenerate.setOnClickListener {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("Generar órdenes automáticamente")
                .setMessage("Se crearán órdenes de compra para todos los productos con stock crítico. ¿Continuar?")
                .setPositiveButton("Generar") { _, _ ->
                    viewModel.autoGenerateOrders(args.businessId, args.branchId)
                }
                .setNegativeButton("Cancelar", null)
                .show()
        }
    }

    private fun setupFilters() {
        binding.chipGroupFilters.setOnCheckedStateChangeListener { _, checkedIds ->
            activeStatusFilter = when {
                checkedIds.contains(R.id.chipPending) -> PurchaseOrderStatus.PENDING
                checkedIds.contains(R.id.chipSent) -> PurchaseOrderStatus.SENT
                checkedIds.contains(R.id.chipReceived) -> PurchaseOrderStatus.RECEIVED
                checkedIds.contains(R.id.chipCancelled) -> PurchaseOrderStatus.CANCELLED
                else -> null
            }
            loadOrders()
        }
    }

    private fun loadOrders() {
        viewModel.loadOrders(args.businessId, status = activeStatusFilter)
    }

    private fun observeOrderListState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.orderListState.collect { state ->
                    when (state) {
                        is Result.Loading -> showLoading(true)
                        is Result.Success -> {
                            showLoading(false)
                            if (state.data.isEmpty()) showEmptyState() else showList(state.data)
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

    private fun observeAutoGenerateState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.autoGenerateState.collect { state ->
                    when (state) {
                        is Result.Loading -> showLoading(true)
                        is Result.Success -> {
                            showLoading(false)
                            val count = state.data.size
                            val msg = if (count > 0) {
                                "Se crearon $count orden(es) de compra automáticamente"
                            } else {
                                "No hay productos con stock crítico"
                            }
                            Snackbar.make(binding.root, msg, Snackbar.LENGTH_LONG).show()
                            viewModel.resetAutoGenerateState()
                            loadOrders()
                        }
                        is Result.Error -> {
                            showLoading(false)
                            Snackbar.make(binding.root, state.message, Snackbar.LENGTH_LONG).show()
                            viewModel.resetAutoGenerateState()
                        }
                        null -> {}
                    }
                }
            }
        }
    }

    private fun showLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
    }

    private fun showEmptyState() {
        binding.layoutEmptyState.visibility = View.VISIBLE
        binding.rvOrders.visibility = View.GONE
    }

    private fun showList(orders: List<com.caas.app.data.model.PurchaseOrder>) {
        binding.layoutEmptyState.visibility = View.GONE
        binding.rvOrders.visibility = View.VISIBLE
        adapter.submitList(orders)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
