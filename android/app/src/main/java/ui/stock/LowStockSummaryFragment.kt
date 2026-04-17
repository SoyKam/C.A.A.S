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
import androidx.recyclerview.widget.LinearLayoutManager
import com.caas.app.core.result.Result
import com.caas.app.data.model.StockAlert
import com.caas.app.databinding.FragmentLowStockSummaryBinding
import com.caas.app.ui.stock.adapter.LowStockSummaryAdapter
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch

class LowStockSummaryFragment : Fragment() {

    private var _binding: FragmentLowStockSummaryBinding? = null
    private val binding get() = _binding!!

    private val viewModel: StockViewModel by activityViewModels()
    private val args: LowStockSummaryFragmentArgs by navArgs()
    private lateinit var adapter: LowStockSummaryAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLowStockSummaryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupClickListeners()
        observeStates()
        binding.btnViewAlerts.setOnClickListener {
            findNavController().navigate(
                LowStockSummaryFragmentDirections.actionLowStockSummaryToStockAlerts(
                    businessId = args.businessId,
                    branchId = ""
                )
            )
        }
    }

    override fun onStart() {
        super.onStart()
        viewModel.checkAndSaveAlerts(args.businessId)
        viewModel.getUnreadAlerts(args.businessId)
    }

    private fun setupClickListeners() {
        binding.btnBack.setOnClickListener { findNavController().navigateUp() }
    }

    private fun setupRecyclerView() {
        adapter = LowStockSummaryAdapter { alert ->
            viewModel.markAlertAsRead(args.businessId, alert.id)
        }
        binding.rvLowStock.layoutManager = LinearLayoutManager(requireContext())
        binding.rvLowStock.adapter = adapter
    }

    private fun observeStates() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.unreadAlertsState.collect { state ->
                    when (state) {
                        is Result.Loading -> showLoading(true)
                        is Result.Success -> {
                            showLoading(false)
                            if (state.data.isEmpty()) showEmptyState()
                            else showAlerts(state.data)
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

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.markReadState.collect { state ->
                    if (state is Result.Success) {
                        viewModel.resetMarkReadState()
                        viewModel.getUnreadAlerts(args.businessId)
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
        binding.rvLowStock.visibility = View.GONE
        binding.btnViewAlerts.visibility = View.GONE
    }

    private fun showAlerts(alerts: List<StockAlert>) {
        binding.tvEmptyState.visibility = View.GONE
        binding.rvLowStock.visibility = View.VISIBLE
        binding.btnViewAlerts.visibility = View.VISIBLE

        val items = alerts
            .sortedBy { it.branchName }
            .groupBy { it.branchName }
            .flatMap { (branchName, branchAlerts) ->
                listOf(
                    LowStockSummaryAdapter.LowStockListItem.BranchHeader(
                        branchName, branchAlerts.size
                    )
                ) + branchAlerts.map {
                    LowStockSummaryAdapter.LowStockListItem.AlertItem(it)
                }
            }
        adapter.submitList(items)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
