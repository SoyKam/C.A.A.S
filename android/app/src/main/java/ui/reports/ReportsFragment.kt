package com.caas.app.ui.reports

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.caas.app.core.result.Result
import com.caas.app.data.model.StockMovement
import com.caas.app.databinding.FragmentReportsBinding
import com.caas.app.ui.business.BusinessViewModel
import com.caas.app.ui.stock.StockViewModel
import com.caas.app.ui.stock.adapter.StockMovementsAdapter
import kotlinx.coroutines.launch

class ReportsFragment : Fragment() {

    private var _binding: FragmentReportsBinding? = null
    private val binding get() = _binding!!

    private val businessViewModel: BusinessViewModel by activityViewModels()
    private val stockViewModel: StockViewModel by activityViewModels()
    private lateinit var adapter: StockMovementsAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentReportsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        observeStates()
    }

    override fun onStart() {
        super.onStart()
        businessViewModel.getBusinessesByOwner()
    }

    private fun setupRecyclerView() {
        adapter = StockMovementsAdapter { /* no detail navigation from reports view */ }
        binding.rvRecentMovements.layoutManager = LinearLayoutManager(requireContext())
        binding.rvRecentMovements.adapter = adapter
    }

    private fun observeStates() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    businessViewModel.businessListState.collect { state ->
                        if (state is Result.Success && state.data.isNotEmpty()) {
                            val ids = state.data.map { it.id }
                            stockViewModel.loadRecentMovements(ids)
                        }
                    }
                }
                launch {
                    stockViewModel.recentMovementsState.collect { state ->
                        when (state) {
                            is Result.Loading -> showLoading(true)
                            is Result.Success -> {
                                showLoading(false)
                                if (state.data.isEmpty()) showEmptyState()
                                else showMovements(state.data)
                            }
                            is Result.Error -> {
                                showLoading(false)
                                showEmptyState()
                            }
                            null -> Unit
                        }
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
        binding.rvRecentMovements.visibility = View.GONE
    }

    private fun showMovements(movements: List<StockMovement>) {
        binding.tvEmptyState.visibility = View.GONE
        binding.rvRecentMovements.visibility = View.VISIBLE
        adapter.submitList(movements)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
