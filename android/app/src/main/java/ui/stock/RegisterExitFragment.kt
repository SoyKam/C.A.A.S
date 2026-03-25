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
import com.caas.app.R
import com.caas.app.core.result.Result
import com.caas.app.data.model.MovementType
import com.caas.app.databinding.FragmentRegisterExitBinding
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch

class RegisterExitFragment : Fragment() {

    private var _binding: FragmentRegisterExitBinding? = null
    private val binding get() = _binding!!

    private val viewModel: StockViewModel by activityViewModels()
    private val args: RegisterExitFragmentArgs by navArgs()

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
        observeExitState()
    }

    private fun setupClickListeners() {
        binding.btnRegisterExit.setOnClickListener {
            val productName = binding.etProductName.text.toString().trim()
            val quantityText = binding.etQuantity.text.toString().trim()
            val reason = binding.etReason.text.toString().trim()

            if (productName.isBlank()) {
                Snackbar.make(binding.root, "El nombre del producto es requerido", Snackbar.LENGTH_SHORT).show()
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

            val productId = productName.lowercase().replace(Regex("[^a-z0-9]"), "_")

            viewModel.registerExit(
                businessId = args.businessId,
                branchId = args.branchId,
                productId = productId,
                productName = productName,
                quantity = quantity,
                type = type,
                reason = reason
            )
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
