package com.caas.app.ui.inventory

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.caas.app.core.result.Result
import com.caas.app.data.model.Branch
import com.caas.app.data.repository.BranchRepositoryImpl
import com.caas.app.data.source.FirestoreBranchDataSource
import com.caas.app.databinding.FragmentInventorySummaryBinding
import com.caas.app.domain.model.InventorySummaryItem
import com.caas.app.ui.inventory.adapter.InventorySummaryAdapter
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch

class InventorySummaryFragment : Fragment() {

    private var _binding: FragmentInventorySummaryBinding? = null
    private val binding get() = _binding!!

    private val viewModel: InventoryViewModel by activityViewModels()
    private val args: InventorySummaryFragmentArgs by navArgs()
    private lateinit var adapter: InventorySummaryAdapter

    private val branchRepository by lazy {
        BranchRepositoryImpl(FirestoreBranchDataSource(FirebaseFirestore.getInstance()))
    }

    private var branches: List<Branch> = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentInventorySummaryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        observeSummaryState()
        loadBranches()
    }

    private fun setupRecyclerView() {
        adapter = InventorySummaryAdapter()
        binding.rvInventory.layoutManager = LinearLayoutManager(requireContext())
        binding.rvInventory.adapter = adapter
    }

    private fun loadBranches() {
        viewLifecycleOwner.lifecycleScope.launch {
            when (val result = branchRepository.getBranchesByBusinessId(args.businessId)) {
                is Result.Success -> {
                    branches = result.data.filter { it.isActive }
                    setupBranchSpinner()
                }
                is Result.Error -> {
                    Snackbar.make(binding.root, result.message, Snackbar.LENGTH_LONG).show()
                }
                is Result.Loading -> Unit
            }
        }
    }

    private fun setupBranchSpinner() {
        if (branches.isEmpty()) {
            Snackbar.make(binding.root, "No hay sucursales activas", Snackbar.LENGTH_LONG).show()
            return
        }

        val branchNames = branches.map { it.name }
        val spinnerAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            branchNames
        ).also { it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) }

        binding.spinnerBranch.adapter = spinnerAdapter
        binding.spinnerBranch.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val branchId = branches[position].id
                viewModel.getInventorySummary(args.businessId, branchId)
            }
            override fun onNothingSelected(parent: AdapterView<*>?) = Unit
        }
    }

    private fun observeSummaryState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.summaryState.collect { state ->
                    when (state) {
                        is Result.Loading -> showLoading(true)
                        is Result.Success -> {
                            showLoading(false)
                            if (state.data.isEmpty()) showEmptyState() else showInventory(state.data)
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
        binding.rvInventory.visibility = View.GONE
    }

    private fun showInventory(items: List<InventorySummaryItem>) {
        binding.tvEmptyState.visibility = View.GONE
        binding.rvInventory.visibility = View.VISIBLE
        adapter.submitList(items)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        viewModel.resetSummaryState()
        _binding = null
    }
}
