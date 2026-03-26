package com.caas.app.ui.stock

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
import com.caas.app.core.result.Result
import com.caas.app.databinding.FragmentRegisterEntryBinding
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch

class RegisterEntryFragment : Fragment() {

    private var _binding: FragmentRegisterEntryBinding? = null
    private val binding get() = _binding!!

    private val viewModel: StockViewModel by activityViewModels()
    private val args: RegisterEntryFragmentArgs by navArgs()

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
        observeEntryState()
    }

    private fun setupClickListeners() {
        binding.btnRegisterEntry.setOnClickListener {
            val productName = binding.etProductName.text.toString().trim()
            val quantityText = binding.etQuantity.text.toString().trim()
            val minStockText = binding.etMinStock.text.toString().trim()

            if (productName.isBlank()) {
                Snackbar.make(binding.root, "El nombre del producto es requerido", Snackbar.LENGTH_SHORT).show()
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

            val productId = productName.lowercase().replace(Regex("[^a-z0-9]"), "_")

            viewModel.registerEntry(
                businessId = args.businessId,
                branchId = args.branchId,
                productId = productId,
                productName = productName,
                quantity = quantity,
                minStock = minStock
            )
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
