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
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.caas.app.core.result.Result
import com.caas.app.data.model.Stock
import com.caas.app.databinding.FragmentStockAlertsBinding
import com.caas.app.ui.stock.adapter.StockAlertAdapter
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch

class StockAlertsFragment : Fragment() {

    private var _binding: FragmentStockAlertsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: StockViewModel by activityViewModels()
    private val args: StockAlertsFragmentArgs by navArgs()
    private lateinit var adapter: StockAlertAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStockAlertsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        observeLowStockState()
    }

    override fun onStart() {
        super.onStart()
        viewModel.getLowStock(args.businessId, args.branchId)
    }

    private fun setupRecyclerView() {
        adapter = StockAlertAdapter()
        binding.rvAlertList.layoutManager = LinearLayoutManager(requireContext())
        binding.rvAlertList.adapter = adapter
    }

    private fun observeLowStockState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.lowStockState.collect { state ->
                    when (state) {
                        is Result.Loading -> showLoading(true)
                        is Result.Success -> {
                            showLoading(false)
                            if (state.data.isEmpty()) showEmptyState() else showAlerts(state.data)
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
        binding.rvAlertList.visibility = View.GONE
    }

    private fun showAlerts(stock: List<Stock>) {
        binding.tvEmptyState.visibility = View.GONE
        binding.rvAlertList.visibility = View.VISIBLE
        adapter.submitList(stock)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
