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
import androidx.recyclerview.widget.LinearLayoutManager
import com.caas.app.core.result.Result
import com.caas.app.data.model.Branch
import com.caas.app.databinding.FragmentBranchListBinding
import com.caas.app.ui.business.adapter.BranchListAdapter
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch

class BranchListFragment : Fragment() {

    private var _binding: FragmentBranchListBinding? = null
    private val binding get() = _binding!!

    private val viewModel: BranchViewModel by activityViewModels()
    private val args: BranchListFragmentArgs by navArgs()
    private lateinit var adapter: BranchListAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBranchListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupClickListeners()
        observeBranchListState()
    }

    override fun onStart() {
        super.onStart()
        viewModel.getBranchesByBusiness(args.businessId)
    }

    private fun setupRecyclerView() {
        adapter = BranchListAdapter { branchId ->
            findNavController().navigate(
                BranchListFragmentDirections.actionBranchListToEditBranch(args.businessId, branchId)
            )
        }
        binding.rvBranchList.layoutManager = LinearLayoutManager(requireContext())
        binding.rvBranchList.adapter = adapter
    }

    private fun setupClickListeners() {
        binding.btnAddBranch.setOnClickListener {
            findNavController().navigate(
                BranchListFragmentDirections.actionBranchListToCreateBranch(args.businessId)
            )
        }
    }

    private fun observeBranchListState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.branchListState.collect { state ->
                    when (state) {
                        is Result.Loading -> showLoading(true)
                        is Result.Success -> {
                            showLoading(false)
                            if (state.data.isEmpty()) showEmptyState() else showBranches(state.data)
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
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
    }

    private fun showEmptyState() {
        binding.tvEmptyState.visibility = View.VISIBLE
        binding.rvBranchList.visibility = View.GONE
    }

    private fun showBranches(branches: List<Branch>) {
        binding.tvEmptyState.visibility = View.GONE
        binding.rvBranchList.visibility = View.VISIBLE
        adapter.submitList(branches)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
