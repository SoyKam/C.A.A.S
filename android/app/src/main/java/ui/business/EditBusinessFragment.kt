package com.caas.app.ui.business

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
import com.caas.app.core.result.Result
import com.caas.app.databinding.FragmentEditBusinessBinding
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch

/**
 * Fragmento para editar información de un negocio existente.
 * Captura cambios en nombre, sector e identificación fiscal.
 */
class EditBusinessFragment : Fragment() {

    private var _binding: FragmentEditBusinessBinding? = null
    private val binding get() = _binding!!

    private val viewModel: BusinessViewModel by activityViewModels()

    // TODO: Obtener businessId del Bundle o argumentos de navegación
    private val businessId: String = ""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEditBusinessBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupClickListeners()
        observeUpdateBusinessState()
    }

    private fun setupClickListeners() {
        binding.btnBack.setOnClickListener { findNavController().navigateUp() }

        binding.btnUpdateBusiness.setOnClickListener {
            val name = binding.etBusinessName.text.toString().trim()
            val sector = binding.etSector.text.toString().trim()
            val taxId = binding.etTaxId.text.toString().trim()
            viewModel.updateBusiness(businessId, name, sector, taxId)
        }
    }

    private fun observeUpdateBusinessState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.updateBusinessState.collect { state ->
                    when (state) {
                        is Result.Loading -> showLoading(true)
                        is Result.Success<*> -> {
                            showLoading(false)
                            showSuccess("Negocio actualizado correctamente")
                            viewModel.resetUpdateState()
                        }
                        is Result.Error -> {
                            showLoading(false)
                            showError(state.message)
                        }
                        null -> showLoading(false)
                    }
                }
            }
        }
    }

    private fun showLoading(isLoading: Boolean) {
        binding.btnUpdateBusiness.isEnabled = !isLoading
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
    }

    private fun showError(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
    }

    private fun showSuccess(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}