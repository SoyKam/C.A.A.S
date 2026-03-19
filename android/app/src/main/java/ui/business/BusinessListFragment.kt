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
import com.caas.app.data.model.Business
import com.caas.app.databinding.FragmentBusinessListBinding
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch

class BusinessListFragment : Fragment() {

    private var _binding: FragmentBusinessListBinding? = null
    private val binding get() = _binding!!

    private val viewModel: BusinessViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBusinessListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupClickListeners()
        observeBusinessListState()
        viewModel.getBusinessesByOwner()
    }

    private fun setupClickListeners() {
        binding.btnCreateNewBusiness.setOnClickListener {
            findNavController().navigate(
                com.caas.app.R.id.action_businessList_to_createBusiness
            )
        }
    }

    private fun observeBusinessListState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.businessListState.collect { state ->
                    when (state) {
                        is Result.Loading -> showLoading(true)
                        is Result.Success -> {
                            showLoading(false)
                            val businesses = state.data
                            if (businesses.isEmpty()) {
                                showEmptyState()
                            } else {
                                showBusinesses(businesses)
                            }
                        }
                        is Result.Error -> {
                            showLoading(false)
                            showError(state.message)
                            showCreateButton()
                        }
                        null -> showLoading(false)
                    }
                }
            }
        }
    }

    private fun showLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
    }

    private fun showEmptyState() {
        binding.tvEmptyState.visibility = View.VISIBLE
        binding.rvBusinessList.visibility = View.GONE
        binding.btnCreateNewBusiness.visibility = View.VISIBLE
    }

    private fun showBusinesses(@Suppress("UNUSED_PARAMETER") businesses: List<Business>) {
        binding.tvEmptyState.visibility = View.GONE
        binding.rvBusinessList.visibility = View.VISIBLE
        binding.btnCreateNewBusiness.visibility = View.VISIBLE
        // TODO: Implement RecyclerView adapter
    }

    private fun showCreateButton() {
        binding.btnCreateNewBusiness.visibility = View.VISIBLE
    }

    private fun showError(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}