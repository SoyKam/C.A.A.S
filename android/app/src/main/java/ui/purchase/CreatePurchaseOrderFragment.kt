package com.caas.app.ui.purchase

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.caas.app.core.result.Result
import com.caas.app.data.model.Provider
import com.caas.app.data.model.PurchaseOrderItem
import com.caas.app.data.model.Stock
import com.caas.app.databinding.DialogSelectProductBinding
import com.caas.app.databinding.FragmentCreatePurchaseOrderBinding
import com.caas.app.ui.provider.ProviderViewModel
import com.caas.app.ui.purchase.adapter.OrderProductAdapter
import com.caas.app.ui.purchase.adapter.SelectableStockAdapter
import com.caas.app.ui.stock.StockViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch

class CreatePurchaseOrderFragment : Fragment() {

    private var _binding: FragmentCreatePurchaseOrderBinding? = null
    private val binding get() = _binding!!

    private val viewModel: PurchaseOrderViewModel by activityViewModels()
    private val providerViewModel: ProviderViewModel by activityViewModels()
    private val stockViewModel: StockViewModel by activityViewModels()
    private val args: CreatePurchaseOrderFragmentArgs by navArgs()

    private val orderItems = mutableListOf<PurchaseOrderItem>()
    private var providers: List<Provider> = emptyList()
    private var selectedProvider: Provider? = null
    private var stockItems: List<Stock> = emptyList()

    private lateinit var productAdapter: OrderProductAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCreatePurchaseOrderBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupClickListeners()
        observeProviders()
        observeCreateState()
        observeStockState()
        providerViewModel.getProvidersByBusiness(args.businessId)
        stockViewModel.getStockByBranch(args.businessId, args.branchId)
    }

    private fun setupRecyclerView() {
        productAdapter = OrderProductAdapter(
            onQuantityChanged = { item, newQty ->
                val idx = orderItems.indexOfFirst { it.productId == item.productId }
                if (idx >= 0) {
                    orderItems[idx] = item.copy(quantity = newQty)
                    productAdapter.submitList(orderItems.toList())
                }
            },
            onRemove = { item ->
                orderItems.removeAll { it.productId == item.productId }
                productAdapter.submitList(orderItems.toList())
                updateEmptyProductsState()
            }
        )
        binding.rvProducts.layoutManager = LinearLayoutManager(requireContext())
        binding.rvProducts.adapter = productAdapter
    }

    private fun setupClickListeners() {
        binding.btnBack.setOnClickListener { findNavController().navigateUp() }

        binding.btnAddProduct.setOnClickListener {
            if (selectedProvider == null) {
                Snackbar.make(binding.root, "Selecciona un proveedor primero", Snackbar.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            showSelectProductDialog()
        }

        binding.btnCreateOrder.setOnClickListener {
            submitOrder()
        }
    }

    private fun observeProviders() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                providerViewModel.providerListState.collect { state ->
                    when (state) {
                        is Result.Success -> setupProviderDropdown(state.data)
                        is Result.Error -> Snackbar.make(binding.root, state.message, Snackbar.LENGTH_LONG).show()
                        else -> {}
                    }
                }
            }
        }
    }

    private fun observeStockState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                stockViewModel.stockListState.collect { state ->
                    if (state is Result.Success) {
                        stockItems = state.data
                    }
                }
            }
        }
    }

    private fun observeCreateState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.createOrderState.collect { state ->
                    when (state) {
                        is Result.Loading -> showLoading(true)
                        is Result.Success -> {
                            showLoading(false)
                            viewModel.resetCreateState()
                            Snackbar.make(binding.root, "Orden creada exitosamente", Snackbar.LENGTH_SHORT).show()
                            findNavController().navigateUp()
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

    private fun setupProviderDropdown(providerList: List<Provider>) {
        providers = providerList
        val names = providerList.map { it.name }
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, names)
        binding.actvProvider.setAdapter(adapter)
        binding.actvProvider.setOnItemClickListener { _, _, position, _ ->
            selectedProvider = providers[position]
        }
    }

    private fun showSelectProductDialog() {
        val dialogBinding = DialogSelectProductBinding.inflate(layoutInflater)
        var selectedStock: Stock? = null
        var filteredList = stockItems.toList()

        val stockAdapter = SelectableStockAdapter { stock ->
            selectedStock = stock
            dialogBinding.etQuantity.setText((stock.minStock - stock.quantity).coerceAtLeast(1).toString())
        }
        dialogBinding.rvStockItems.layoutManager = LinearLayoutManager(requireContext())
        dialogBinding.rvStockItems.adapter = stockAdapter
        stockAdapter.submitList(filteredList)

        dialogBinding.etSearch.addTextChangedListener { text ->
            filteredList = if (text.isNullOrBlank()) {
                stockItems
            } else {
                stockItems.filter { it.productName.contains(text, ignoreCase = true) }
            }
            stockAdapter.submitList(filteredList)
        }

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Seleccionar Producto")
            .setView(dialogBinding.root)
            .setPositiveButton("Agregar") { _, _ ->
                val stock = selectedStock ?: run {
                    Snackbar.make(binding.root, "Selecciona un producto", Snackbar.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                val qty = dialogBinding.etQuantity.text.toString().toIntOrNull() ?: 0
                if (qty <= 0) {
                    Snackbar.make(binding.root, "La cantidad debe ser mayor a 0", Snackbar.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                val existing = orderItems.indexOfFirst { it.productId == stock.productId }
                if (existing >= 0) {
                    orderItems[existing] = orderItems[existing].copy(quantity = orderItems[existing].quantity + qty)
                } else {
                    orderItems.add(PurchaseOrderItem(stock.productId, stock.productName, qty, 0.0))
                }
                productAdapter.submitList(orderItems.toList())
                updateEmptyProductsState()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun submitOrder() {
        val provider = selectedProvider
        if (provider == null) {
            Snackbar.make(binding.root, "Selecciona un proveedor", Snackbar.LENGTH_SHORT).show()
            return
        }
        if (orderItems.isEmpty()) {
            Snackbar.make(binding.root, "Agrega al menos un producto", Snackbar.LENGTH_SHORT).show()
            return
        }
        val notes = binding.etNotes.text.toString().trim()
        viewModel.createOrder(args.businessId, args.branchId, provider.id, provider.name, orderItems.toList(), notes)
    }

    private fun updateEmptyProductsState() {
        binding.tvEmptyProducts.visibility = if (orderItems.isEmpty()) View.VISIBLE else View.GONE
    }

    private fun showLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.btnCreateOrder.isEnabled = !isLoading
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
