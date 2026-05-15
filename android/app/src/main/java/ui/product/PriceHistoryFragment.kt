package com.caas.app.ui.product

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.caas.app.R
import com.caas.app.core.result.Result
import com.caas.app.data.model.PriceHistory
import com.caas.app.databinding.FragmentPriceHistoryBinding
import com.caas.app.ui.product.adapter.PriceHistoryAdapter
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class PriceHistoryFragment : Fragment() {

    private var _binding: FragmentPriceHistoryBinding? = null
    private val binding get() = _binding!!

    private val viewModel: PriceHistoryViewModel by viewModels()
    private val args: PriceHistoryFragmentArgs by navArgs()
    private val adapter = PriceHistoryAdapter()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPriceHistoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        observePriceHistory()
        viewModel.getPriceHistory(args.businessId, args.productId)
    }

    private fun setupRecyclerView() {
        binding.rvPriceHistory.layoutManager = LinearLayoutManager(requireContext())
        binding.rvPriceHistory.adapter = adapter
    }

    private fun observePriceHistory() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.priceHistoryState.collect { state ->
                    when (state) {
                        is Result.Loading -> showLoading(true)
                        is Result.Success -> {
                            showLoading(false)
                            handleData(state.data)
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

    private fun handleData(history: List<PriceHistory>) {
        if (history.isEmpty()) {
            binding.tvEmptyState.visibility = View.VISIBLE
            binding.cardChart.visibility = View.GONE
            binding.cardHistory.visibility = View.GONE
            return
        }

        binding.tvEmptyState.visibility = View.GONE
        binding.cardHistory.visibility = View.VISIBLE
        adapter.submitList(history)

        val sortedAsc = history.sortedBy { it.changedAt }
        if (sortedAsc.size >= 2) {
            binding.cardChart.visibility = View.VISIBLE
            setupChart(sortedAsc)
        } else {
            binding.cardChart.visibility = View.GONE
        }
    }

    private fun setupChart(history: List<PriceHistory>) {
        val dateFormat = SimpleDateFormat("dd/MM", Locale.getDefault())
        val labels = history.map { dateFormat.format(Date(it.changedAt)) }

        val saleEntries = history.mapIndexed { i, h -> Entry(i.toFloat(), h.newSalePrice.toFloat()) }
        val costEntries = history.mapIndexed { i, h -> Entry(i.toFloat(), h.newCostPrice.toFloat()) }

        val saleDataSet = LineDataSet(saleEntries, "Precio Venta").apply {
            color = ContextCompat.getColor(requireContext(), R.color.button_orange)
            setCircleColor(ContextCompat.getColor(requireContext(), R.color.button_orange))
            lineWidth = 2f
            circleRadius = 4f
            setDrawValues(false)
            mode = LineDataSet.Mode.LINEAR
        }

        val costDataSet = LineDataSet(costEntries, "Precio Costo").apply {
            color = ContextCompat.getColor(requireContext(), R.color.blue)
            setCircleColor(ContextCompat.getColor(requireContext(), R.color.blue))
            lineWidth = 2f
            circleRadius = 4f
            setDrawValues(false)
            mode = LineDataSet.Mode.LINEAR
        }

        binding.lineChart.apply {
            data = LineData(saleDataSet, costDataSet)
            description.isEnabled = false
            legend.isEnabled = true
            legend.form = Legend.LegendForm.LINE

            xAxis.apply {
                granularity = 1f
                valueFormatter = object : ValueFormatter() {
                    override fun getFormattedValue(value: Float): String {
                        val index = value.toInt()
                        return if (index in labels.indices) labels[index] else ""
                    }
                }
                setDrawGridLines(false)
            }

            axisRight.isEnabled = false
            axisLeft.setDrawGridLines(true)

            setTouchEnabled(true)
            setPinchZoom(false)
            invalidate()
        }
    }

    private fun showLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
