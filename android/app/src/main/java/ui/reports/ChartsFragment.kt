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
import com.caas.app.core.result.Result
import com.caas.app.data.model.MovementType
import com.caas.app.data.model.StockMovement
import com.caas.app.databinding.FragmentChartsBinding
import com.caas.app.ui.business.BusinessViewModel
import com.caas.app.ui.stock.StockViewModel
import kotlinx.coroutines.launch

class ChartsFragment : Fragment() {

    private var _binding: FragmentChartsBinding? = null
    private val binding get() = _binding!!

    private val businessViewModel: BusinessViewModel by activityViewModels()
    private val stockViewModel: StockViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentChartsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observeData()
        loadData()
    }

    private fun loadData() {
        val businessesResult = businessViewModel.businessListState.value
        if (businessesResult is Result.Success) {
            val ids = businessesResult.data.map { it.id }
            stockViewModel.loadRecentMovements(ids) // Reuse existing method or we could add a broader one
        } else {
            businessViewModel.getBusinessesByOwner()
        }
    }

    private fun observeData() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                // If business list updates, reload movements
                launch {
                    businessViewModel.businessListState.collect { state ->
                        if (state is Result.Success) {
                            stockViewModel.loadRecentMovements(state.data.map { it.id })
                        }
                    }
                }

                // Observe movements to update charts
                launch {
                    stockViewModel.recentMovementsState.collect { state ->
                        when (state) {
                            is Result.Loading -> {
                                binding.pbLoading.visibility = View.VISIBLE
                                binding.tvEmptyCharts.visibility = View.GONE
                            }
                            is Result.Success -> {
                                binding.pbLoading.visibility = View.GONE
                                updateCharts(state.data)
                            }
                            is Result.Error -> {
                                binding.pbLoading.visibility = View.GONE
                                binding.tvEmptyCharts.visibility = View.VISIBLE
                                binding.tvEmptyCharts.text = "Error al cargar datos"
                            }
                            null -> Unit
                        }
                    }
                }
            }
        }
    }

    private fun updateCharts(movements: List<StockMovement>) {
        if (movements.isEmpty()) {
            binding.tvEmptyCharts.visibility = View.VISIBLE
            return
        }
        binding.tvEmptyCharts.visibility = View.GONE

        val entries = movements.count { it.type == MovementType.ENTRY }
        val sales = movements.count { it.type == MovementType.SALE }
        val damages = movements.count { it.type == MovementType.DAMAGE }
        val transfers = movements.count { it.type == MovementType.TRANSFER }

        val total = (entries + sales + damages + transfers).toFloat()
        if (total == 0f) {
            binding.tvEmptyCharts.visibility = View.VISIBLE
            return
        }

        binding.tvEntryCount.text = entries.toString()
        binding.tvSaleCount.text = sales.toString()
        binding.tvDamageCount.text = damages.toString()
        binding.tvTransferCount.text = transfers.toString()

        // Update bar widths (weight)
        // Using a simple weight approach since they are in a horizontal layout inside a vertical one.
        // Actually, in the XML I used layout_weight for the View.
        
        setBarWeight(binding.barEntry, entries, total)
        setBarWeight(binding.barSale, sales, total)
        setBarWeight(binding.barDamage, damages, total)
        setBarWeight(binding.barTransfer, transfers, total)
    }

    private fun setBarWeight(view: View, count: Int, total: Float) {
        val params = view.layoutParams as ViewGroup.LayoutParams
        if (view.layoutParams is android.widget.LinearLayout.LayoutParams) {
            val lp = view.layoutParams as android.widget.LinearLayout.LayoutParams
            lp.weight = if (total > 0) (count / total) else 0.01f
            // Ensure minimum visible bar if count > 0
            if (count > 0 && lp.weight < 0.05f) lp.weight = 0.05f
            view.layoutParams = lp
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
