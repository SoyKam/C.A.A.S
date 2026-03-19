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
import androidx.navigation.fragment.navArgs
import com.caas.app.core.result.Result
import com.caas.app.data.model.Business
import com.caas.app.databinding.FragmentBusinessDetailBinding
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch

class BusinessDetailFragment : Fragment() {

    private var _binding: FragmentBusinessDetailBinding? = null
    private val binding get() = _binding!!

    private val viewModel: BusinessViewModel by activityViewModels()
    private val args: BusinessDetailFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBusinessDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupClickListeners()
        observeBusinessState()
        viewModel.getBusiness(args.businessId)
    }

    private fun setupClickListeners() {
        binding.btnEdit.setOnClickListener {
            findNavController().navigate(
                BusinessDetailFragmentDirections.actionBusinessDetailToEditBusiness(args.businessId)
            )
        }

        binding.btnBack.setOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun observeBusinessState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.businessState.collect { state ->
                    when (state) {
                        is Result.Loading -> showLoading(true)
                        is Result.Success<*> -> {
                            showLoading(false)
                            val business = state.data as? Business
                            business?.let { displayBusiness(it) }
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

    private fun displayBusiness(business: Business) {
        binding.tvBusinessName.text = business.name
        binding.tvSector.text = business.sector
        binding.tvTaxId.text = business.taxId
    }

    private fun showLoading(isLoading: Boolean) {
        binding.btnEdit.isEnabled = !isLoading
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
    }

    private fun showError(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}