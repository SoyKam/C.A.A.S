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
import com.caas.app.core.result.Result
import com.caas.app.data.model.Product
import com.caas.app.databinding.FragmentRegisterEntryBinding
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch

class RegisterEntryFragment : Fragment() {

    private var _binding: FragmentRegisterEntryBinding? = null
    private val binding get() = _binding!!

    private val viewModel: StockViewModel by activityViewModels()
    private val args: RegisterEntryFragmentArgs by navArgs()

    private var products: List<Product> = emptyList()
    private var selectedProduct: Product? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRegisterEntryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupClickListeners()
        observeProductsState()
        observeEntryState()
        viewModel.getBusinessProducts(args.businessId)
    }

    private fun setupProductDropdown(productList: List<Product>) {
        products = productList
        val names = productList.map { it.name }
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, names)
        binding.actvProduct.setAdapter(adapter)
        binding.actvProduct.setOnItemClickListener { _, _, position, _ ->
            selectedProduct = products[position]
        }
    }

    private fun setupClickListeners() {
        binding.btnRegisterEntry.setOnClickListener {
            val product = selectedProduct
            val quantityText = binding.etQuantity.text.toString().trim()
            val minStockText = binding.etMinStock.text.toString().trim()

            if (product == null) {
                Snackbar.make(binding.root, "Selecciona un producto del catálogo", Snackbar.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (quantityText.isBlank()) {
                Snackbar.make(binding.root, "La cantidad es requerida", Snackbar.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (minStockText.isBlank()) {
                Snackbar.make(binding.root, "El stock mínimo es requerido", Snackbar.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val quantity = quantityText.toIntOrNull()
            if (quantity == null || quantity <= 0) {
                Snackbar.make(binding.root, "La cantidad debe ser un número mayor a 0", Snackbar.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val minStock = minStockText.toIntOrNull()
            if (minStock == null || minStock < 0) {
                Snackbar.make(binding.root, "El stock mínimo debe ser un número válido", Snackbar.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            viewModel.registerEntry(
                businessId = args.businessId,
                branchId = args.branchId,
                productId = product.id,
                productName = product.name,
                quantity = quantity,
                minStock = minStock
            )
        }
    }

    private fun observeProductsState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.businessProductsState.collect { state ->
                    when (state) {
                        is Result.Loading -> showLoading(true)
                        is Result.Success -> {
                            showLoading(false)
                            if (state.data.isEmpty()) {
                                Snackbar.make(binding.root, "No hay productos en el catálogo del negocio", Snackbar.LENGTH_LONG).show()
                            } else {
                                setupProductDropdown(state.data)
                            }
                        }
                        is Result.Error -> {
                            showLoading(false)
                            Snackbar.make(binding.root, state.message, Snackbar.LENGTH_LONG).show()
                        }
                        null -> {}
                    }
                }
            }
        }
    }

    private fun observeEntryState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.stockEntryState.collect { state ->
                    when (state) {
                        is Result.Loading -> showLoading(true)
                        is Result.Success -> {
                            showLoading(false)
                            viewModel.resetEntryState()
                            Snackbar.make(binding.root, "Entrada registrada exitosamente", Snackbar.LENGTH_SHORT).show()
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
        binding.btnRegisterEntry.isEnabled = !isLoading
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
