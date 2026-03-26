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
import com.caas.app.data.model.Branch
import com.caas.app.databinding.FragmentBranchDetailBinding
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch

class BranchDetailFragment : Fragment() {

    private var _binding: FragmentBranchDetailBinding? = null
    private val binding get() = _binding!!

    private val viewModel: BranchViewModel by activityViewModels()
    private val args: BranchDetailFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBranchDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupClickListeners()
        observeBranchState()
        viewModel.getBranchById(args.businessId, args.branchId)
    }

    private fun setupClickListeners() {
        binding.btnEdit.setOnClickListener {
            findNavController().navigate(
                BranchDetailFragmentDirections.actionBranchDetailToEditBranch(
                    args.businessId, args.branchId
                )
            )
        }

        binding.btnViewStock.setOnClickListener {
            findNavController().navigate(
                BranchDetailFragmentDirections.actionBranchDetailToStockList(
                    args.businessId, args.branchId
                )
            )
        }

        binding.btnBack.setOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun observeBranchState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.branchState.collect { state ->
                    when (state) {
                        is Result.Loading -> showLoading(true)
                        is Result.Success -> {
                            showLoading(false)
                            displayBranch(state.data)
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

    private fun displayBranch(branch: Branch) {
        binding.tvBranchName.text = branch.name
        binding.tvBranchAddress.text = branch.address
        binding.tvBranchPhone.text = branch.phone
    }

    private fun showLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
