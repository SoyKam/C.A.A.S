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
import com.caas.app.data.model.Stock
import com.caas.app.databinding.FragmentStockListBinding
import com.caas.app.ui.stock.adapter.StockListAdapter
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch

class StockListFragment : Fragment() {

    private var _binding: FragmentStockListBinding? = null
    private val binding get() = _binding!!

    private val viewModel: StockViewModel by activityViewModels()
    private val args: StockListFragmentArgs by navArgs()
    private lateinit var adapter: StockListAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStockListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupClickListeners()
        observeStockListState()
    }

    override fun onStart() {
        super.onStart()
        viewModel.getStockByBranch(args.businessId, args.branchId)
    }

    private fun setupRecyclerView() {
        adapter = StockListAdapter { stock ->
            Snackbar.make(
                binding.root,
                "Movimientos de ${stock.productName}: ${stock.quantity} en stock",
                Snackbar.LENGTH_LONG
            ).show()
        }
        binding.rvStockList.layoutManager = LinearLayoutManager(requireContext())
        binding.rvStockList.adapter = adapter
    }

    private fun setupClickListeners() {
        binding.btnBack.setOnClickListener { findNavController().navigateUp() }

        binding.btnRegisterEntry.setOnClickListener {
            findNavController().navigate(
                StockListFragmentDirections.actionStockListToRegisterEntry(
                    args.businessId, args.branchId
                )
            )
        }

        binding.btnRegisterExit.setOnClickListener {
            findNavController().navigate(
                StockListFragmentDirections.actionStockListToRegisterExit(
                    args.businessId, args.branchId
                )
            )
        }

        binding.btnViewAlerts.setOnClickListener {
            findNavController().navigate(
                StockListFragmentDirections.actionStockListToStockAlerts(
                    args.businessId, args.branchId
                )
            )
        }

        binding.btnViewMovements.setOnClickListener {
            findNavController().navigate(
                StockListFragmentDirections.actionStockListToStockMovements(
                    args.businessId, args.branchId
                )
            )
        }
    }

    private fun observeStockListState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.stockListState.collect { state ->
                    when (state) {
                        is Result.Loading -> showLoading(true)
                        is Result.Success -> {
                            showLoading(false)
                            if (state.data.isEmpty()) showEmptyState() else showStock(state.data)
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
        binding.rvStockList.visibility = View.GONE
    }

    private fun showStock(stock: List<Stock>) {
        binding.tvEmptyState.visibility = View.GONE
        binding.rvStockList.visibility = View.VISIBLE
        adapter.submitList(stock)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
