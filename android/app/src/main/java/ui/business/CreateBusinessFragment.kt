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
import com.caas.app.databinding.FragmentCreateBusinessBinding
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch

class CreateBusinessFragment : Fragment() {

    private var _binding: FragmentCreateBusinessBinding? = null
    private val binding get() = _binding!!

    private val viewModel: BusinessViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCreateBusinessBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupClickListeners()
        observeCreateBusinessState()
    }

    private fun setupClickListeners() {
        binding.btnBack.setOnClickListener { findNavController().navigateUp() }

        binding.btnCreateBusiness.setOnClickListener {
            val name = binding.etBusinessName.text.toString().trim()
            val sector = binding.etSector.text.toString().trim()
            val taxId = binding.etTaxId.text.toString().trim()

            // Validación antes de llamar ViewModel
            if (name.isEmpty() || sector.isEmpty() || taxId.isEmpty()) {
                Snackbar.make(binding.root, "Todos los campos son requeridos", Snackbar.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            viewModel.createBusiness(name, sector, taxId)
        }
    }

    private fun observeCreateBusinessState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.createBusinessState.collect { state ->
                    when (state) {
                        is Result.Loading -> showLoading(true)
                        is Result.Success<*> -> {
                            showLoading(false)
                            showSuccess("Negocio creado correctamente")
                            viewModel.resetCreateState()
                            // Navegar a BusinessList DESPUÉS de resetear
                            findNavController().navigate(
                                com.caas.app.R.id.action_createBusiness_to_businessList
                            )
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
        binding.btnCreateBusiness.isEnabled = !isLoading
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