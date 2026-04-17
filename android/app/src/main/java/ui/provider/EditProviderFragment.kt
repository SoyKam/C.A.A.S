package com.caas.app.ui.provider

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
import com.caas.app.data.model.Provider
import com.caas.app.databinding.FragmentEditProviderBinding
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch

class EditProviderFragment : Fragment() {

    private var _binding: FragmentEditProviderBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ProviderViewModel by activityViewModels()
    private val args: EditProviderFragmentArgs by navArgs()
    private var currentProvider: Provider? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEditProviderBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupClickListeners()
        observeProviderState()
        observeUpdateState()
        observeDeleteState()
        viewModel.getProviderById(args.businessId, args.providerId)
    }

    private fun setupClickListeners() {
        binding.btnBack.setOnClickListener { findNavController().navigateUp() }

        binding.btnSave.setOnClickListener {
            val provider = currentProvider ?: return@setOnClickListener
            val name = binding.etName.text?.toString()?.trim() ?: ""
            val phone = binding.etPhone.text?.toString()?.trim() ?: ""
            val email = binding.etEmail.text?.toString()?.trim() ?: ""
            viewModel.updateProvider(
                args.businessId, args.providerId,
                name, phone, email,
                provider.productIds
            )
        }

        binding.btnDelete.setOnClickListener {
            viewModel.deleteProvider(args.businessId, args.providerId)
        }
    }

    private fun observeProviderState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.providerState.collect { state ->
                    when (state) {
                        is Result.Loading -> showLoading(true)
                        is Result.Success -> {
                            showLoading(false)
                            state.data?.let { preloadData(it) }
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

    private fun observeUpdateState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.updateProviderState.collect { state ->
                    when (state) {
                        is Result.Loading -> showLoading(true)
                        is Result.Success -> {
                            showLoading(false)
                            viewModel.resetUpdateState()
                            findNavController().navigateUp()
                        }
                        is Result.Error -> {
                            showLoading(false)
                            Snackbar.make(binding.root, state.message, Snackbar.LENGTH_LONG).show()
                            viewModel.resetUpdateState()
                        }
                        null -> showLoading(false)
                    }
                }
            }
        }
    }

    private fun observeDeleteState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.deleteProviderState.collect { state ->
                    when (state) {
                        is Result.Loading -> showLoading(true)
                        is Result.Success -> {
                            showLoading(false)
                            viewModel.resetDeleteState()
                            findNavController().navigate(
                                EditProviderFragmentDirections.actionEditProviderToProviderList(args.businessId)
                            )
                        }
                        is Result.Error -> {
                            showLoading(false)
                            Snackbar.make(binding.root, state.message, Snackbar.LENGTH_LONG).show()
                            viewModel.resetDeleteState()
                        }
                        null -> showLoading(false)
                    }
                }
            }
        }
    }

    private fun preloadData(provider: Provider) {
        currentProvider = provider
        binding.etName.setText(provider.name)
        binding.etPhone.setText(provider.phone)
        binding.etEmail.setText(provider.email)
    }

    private fun showLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.btnSave.isEnabled = !isLoading
        binding.btnDelete.isEnabled = !isLoading
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
