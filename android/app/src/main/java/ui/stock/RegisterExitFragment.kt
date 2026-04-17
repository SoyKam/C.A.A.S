package com.caas.app.ui.stock

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.caas.app.R
import com.caas.app.core.result.Result
import com.caas.app.data.model.MovementType
import com.caas.app.data.model.Stock
import com.caas.app.databinding.FragmentRegisterExitBinding
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch

class RegisterExitFragment : Fragment() {

    private var _binding: FragmentRegisterExitBinding? = null
    private val binding get() = _binding!!

    private val viewModel: StockViewModel by activityViewModels()
    private val args: RegisterExitFragmentArgs by navArgs()

    private var stockItems: List<Stock> = emptyList()
    private var selectedStock: Stock? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRegisterExitBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupClickListeners()
        observeStockListForDropdown()
        observeExitState()

        // Load branch stock if not already loaded
        viewModel.getStockByBranch(args.businessId, args.branchId)
    }

    private fun setupStockDropdown(stock: List<Stock>) {
        stockItems = stock
        val names = stock.map { "${it.productName} (${it.quantity} en stock)" }
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, names)
        binding.actvProduct.setAdapter(adapter)
        binding.actvProduct.setOnItemClickListener { _, _, position, _ ->
            selectedStock = stockItems[position]
        }
    }

    private fun setupClickListeners() {
        binding.btnBack.setOnClickListener { findNavController().navigateUp() }

        binding.btnRegisterExit.setOnClickListener {
            val stock = selectedStock
            val quantityText = binding.etQuantity.text.toString().trim()
            val reason = binding.etReason.text.toString().trim()

            if (stock == null) {
                Snackbar.make(binding.root, "Selecciona un producto de la lista", Snackbar.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (quantityText.isBlank()) {
                Snackbar.make(binding.root, "La cantidad es requerida", Snackbar.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val quantity = quantityText.toIntOrNull()
            if (quantity == null || quantity <= 0) {
                Snackbar.make(binding.root, "La cantidad debe ser un número mayor a 0", Snackbar.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val type = when (binding.rgMovementType.checkedRadioButtonId) {
                R.id.rbDamage -> MovementType.DAMAGE
                R.id.rbTransfer -> MovementType.TRANSFER
                else -> MovementType.SALE
            }

            viewModel.registerExit(
                businessId = args.businessId,
                branchId = args.branchId,
                productId = stock.productId,
                productName = stock.productName,
                quantity = quantity,
                type = type,
                reason = reason
            )
        }
    }

    private fun observeStockListForDropdown() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.stockListState.collect { state ->
                    when (state) {
                        is Result.Success -> {
                            if (state.data.isEmpty()) {
                                Snackbar.make(binding.root, "No hay stock registrado en esta sucursal", Snackbar.LENGTH_LONG).show()
                            } else {
                                setupStockDropdown(state.data)
                            }
                        }
                        else -> {}
                    }
                }
            }
        }
    }

    private fun observeExitState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.stockExitState.collect { state ->
                    when (state) {
                        is Result.Loading -> showLoading(true)
                        is Result.Success -> {
                            showLoading(false)
                            viewModel.resetExitState()
                            Snackbar.make(binding.root, "Salida registrada exitosamente", Snackbar.LENGTH_SHORT).show()
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

    private fun showLoading(isLoading: Boolean) {
        binding.btnRegisterExit.isEnabled = !isLoading
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
