package com.caas.app.ui.reports

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.caas.app.R
import com.caas.app.core.result.Result
import com.caas.app.data.model.Branch
import com.caas.app.data.model.Business
import com.caas.app.data.model.MovementType
import com.caas.app.data.model.StockMovement
import com.caas.app.databinding.FragmentChartsBinding
import com.caas.app.ui.business.BusinessViewModel
import com.caas.app.ui.stock.StockViewModel
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.formatter.PercentFormatter
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputLayout
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.*

class ChartsFragment : Fragment() {

    private var _binding: FragmentChartsBinding? = null
    private val binding get() = _binding!!

    private val businessViewModel: BusinessViewModel by activityViewModels()
    private val stockViewModel: StockViewModel by activityViewModels()
    private val reportsViewModel: ReportsViewModel by activityViewModels()

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
        setupChartsStyle()
        observeData()
        setupExportButton()
        observeExportStates()
    }

    override fun onStart() {
        super.onStart()
        refreshData()
    }

    private fun refreshData() {
        val current = businessViewModel.businessListState.value
        if (current is Result.Success && current.data.isNotEmpty()) {
            val businessIds = current.data.map { it.id }
            stockViewModel.loadRecentMovements(businessIds, limit = 0)
        } else {
            businessViewModel.getBusinessesByOwner()
        }
    }

    private fun setupChartsStyle() {
        binding.pieChartMovements.apply {
            setUsePercentValues(true)
            description.isEnabled = false
            setExtraOffsets(5f, 10f, 5f, 5f)
            dragDecelerationFrictionCoef = 0.95f
            isDrawHoleEnabled = true
            setHoleColor(Color.WHITE)
            setTransparentCircleColor(Color.WHITE)
            setTransparentCircleAlpha(110)
            holeRadius = 58f
            transparentCircleRadius = 61f
            setDrawCenterText(true)
            centerText = "Movimientos"
            rotationAngle = 0f
            isRotationEnabled = true
            isHighlightPerTapEnabled = true
            animateY(1400, Easing.EaseInOutQuad)
            legend.isEnabled = true
            setEntryLabelColor(Color.BLACK)
            setEntryLabelTextSize(12f)
        }

        binding.barChartStock.apply {
            description.isEnabled = false
            setFitBars(true)
            xAxis.position = XAxis.XAxisPosition.BOTTOM
            xAxis.setDrawGridLines(false)
            xAxis.granularity = 1f
            axisLeft.setDrawGridLines(true)
            axisRight.isEnabled = false
            animateY(1000)
            legend.isEnabled = false
        }
    }

    private fun observeData() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    businessViewModel.businessListState.collect { state ->
                        if (state is Result.Success && state.data.isNotEmpty() &&
                            stockViewModel.recentMovementsState.value == null) {
                            stockViewModel.loadRecentMovements(state.data.map { it.id }, limit = 0)
                        }
                    }
                }

                launch {
                    stockViewModel.recentMovementsState.collect { state ->
                        when (state) {
                            is Result.Loading -> binding.pbLoading.visibility = View.VISIBLE
                            is Result.Success -> {
                                binding.pbLoading.visibility = View.GONE
                                updateUI(state.data)
                            }
                            is Result.Error -> {
                                binding.pbLoading.visibility = View.GONE
                                Snackbar.make(binding.root, "Error: ${state.message}", Snackbar.LENGTH_LONG).show()
                            }
                            null -> Unit
                        }
                    }
                }
            }
        }
    }

    private fun updateUI(movements: List<StockMovement>) {
        if (movements.isEmpty()) return

        val totalMovements = movements.size
        val entriesCount = movements.count { it.type == MovementType.ENTRY }
        val salesCount = movements.count { it.type == MovementType.SALE }
        val damagesCount = movements.count { it.type == MovementType.DAMAGE }
        val transfersCount = movements.count { it.type == MovementType.TRANSFER }
        
        binding.tvTotalMovements.text = totalMovements.toString()
        binding.tvTotalProducts.text = movements.map { it.productId }.distinct().size.toString()
        
        val totalIn = movements.filter { it.type == MovementType.ENTRY }.sumOf { it.quantity }
        val totalOut = movements.filter { it.type != MovementType.ENTRY }.sumOf { it.quantity }
        binding.tvTotalStock.text = (totalIn - totalOut).coerceAtLeast(0).toString()
        
        binding.tvCriticalProducts.text = movements.filter { it.type == MovementType.DAMAGE }.size.toString()

        updatePieChart(entriesCount, salesCount, damagesCount, transfersCount)
        updateBarChart(movements)
    }

    private fun updatePieChart(entries: Int, sales: Int, damages: Int, transfers: Int) {
        val pieEntries = mutableListOf<PieEntry>()
        if (entries > 0) pieEntries.add(PieEntry(entries.toFloat(), "Entradas"))
        if (sales > 0) pieEntries.add(PieEntry(sales.toFloat(), "Salidas"))
        if (damages > 0) pieEntries.add(PieEntry(damages.toFloat(), "Daños"))
        if (transfers > 0) pieEntries.add(PieEntry(transfers.toFloat(), "Traslados"))

        if (pieEntries.isEmpty()) {
            binding.pieChartMovements.clear()
            return
        }

        val dataSet = PieDataSet(pieEntries, "")
        dataSet.colors = listOf(
            ContextCompat.getColor(requireContext(), R.color.green),
            ContextCompat.getColor(requireContext(), R.color.orange),
            ContextCompat.getColor(requireContext(), R.color.red),
            ContextCompat.getColor(requireContext(), R.color.blue)
        )
        dataSet.sliceSpace = 3f
        dataSet.selectionShift = 5f

        val data = PieData(dataSet)
        data.setValueFormatter(PercentFormatter(binding.pieChartMovements))
        data.setValueTextSize(11f)
        data.setValueTextColor(Color.WHITE)
        
        binding.pieChartMovements.data = data
        binding.pieChartMovements.invalidate()
    }

    private fun updateBarChart(movements: List<StockMovement>) {
        val topProducts = movements.groupBy { it.productName }
            .mapValues { it.value.sumOf { m -> if (m.type == MovementType.ENTRY) m.quantity else -m.quantity } }
            .toList()
            .sortedByDescending { it.second }
            .take(5)

        if (topProducts.isEmpty()) {
            binding.barChartStock.clear()
            return
        }

        val barEntries = mutableListOf<BarEntry>()
        val labels = mutableListOf<String>()

        topProducts.forEachIndexed { index, pair ->
            barEntries.add(BarEntry(index.toFloat(), pair.second.toFloat()))
            labels.add(if (pair.first.length > 8) pair.first.take(8) + ".." else pair.first)
        }

        val dataSet = BarDataSet(barEntries, "Stock Actual")
        dataSet.color = ContextCompat.getColor(requireContext(), R.color.orange)
        dataSet.valueTextColor = ContextCompat.getColor(requireContext(), R.color.text_primary)
        dataSet.valueTextSize = 10f

        val data = BarData(dataSet)
        data.barWidth = 0.5f

        binding.barChartStock.apply {
            this.data = data
            xAxis.valueFormatter = IndexAxisValueFormatter(labels)
            xAxis.labelCount = labels.size
            invalidate()
        }
    }

    private fun setupExportButton() {
        binding.btnExportPdf.setOnClickListener { openExportDialog() }
    }

    private fun openExportDialog() {
        val businessResult = businessViewModel.businessListState.value
        if (businessResult !is Result.Success || businessResult.data.isEmpty()) {
            Snackbar.make(binding.root, "No hay un negocio disponible", Snackbar.LENGTH_SHORT).show()
            return
        }
        val business = businessResult.data.first()
        reportsViewModel.resetBranchesState()
        reportsViewModel.loadBranches(business.id)

        viewLifecycleOwner.lifecycleScope.launch {
            val branchState = reportsViewModel.branchesState
                .first { it is Result.Success || it is Result.Error }
            if (branchState is Result.Error) {
                Snackbar.make(binding.root, branchState.message, Snackbar.LENGTH_SHORT).show()
                return@launch
            }
            val branches = (branchState as Result.Success).data
            if (branches.isEmpty()) {
                Snackbar.make(binding.root, "No hay sucursales disponibles", Snackbar.LENGTH_SHORT).show()
                return@launch
            }
            showExportOptionsDialog(business, branches)
        }
    }

    private fun showExportOptionsDialog(business: Business, branches: List<Branch>) {
        val dialogView = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_export_options, null)

        val actvBranch = dialogView.findViewById<com.google.android.material.textfield.MaterialAutoCompleteTextView>(R.id.actvBranch)
        val tilDateFrom = dialogView.findViewById<TextInputLayout>(R.id.tilDateFrom)
        val tilDateTo = dialogView.findViewById<TextInputLayout>(R.id.tilDateTo)
        val btnGenerate = dialogView.findViewById<MaterialButton>(R.id.btnGenerate)
        val btnCancel = dialogView.findViewById<MaterialButton>(R.id.btnDialogCancel)

        tilDateFrom.visibility = View.GONE
        tilDateTo.visibility = View.GONE

        val branchNames = branches.map { it.name }
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, branchNames)
        actvBranch.setAdapter(adapter)

        var selectedBranch: Branch? = null
        actvBranch.setOnItemClickListener { _, _, position, _ ->
            selectedBranch = branches[position]
        }

        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setView(dialogView)
            .setTitle("Reporte de Inventario")
            .create()

        btnCancel.setOnClickListener { dialog.dismiss() }
        btnGenerate.setOnClickListener {
            val branch = selectedBranch ?: run {
                Snackbar.make(binding.root, "Selecciona una sucursal", Snackbar.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            dialog.dismiss()
            reportsViewModel.exportInventoryPdf(
                context = requireContext(),
                businessId = business.id,
                businessName = business.name,
                branchId = branch.id,
                branchName = branch.name
            )
        }
        dialog.show()
    }

    private fun observeExportStates() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                reportsViewModel.exportPdfState.collect { state ->
                    when (state) {
                        is Result.Loading -> {
                            binding.progressExportPdf.visibility = View.VISIBLE
                            binding.btnExportPdf.isEnabled = false
                        }
                        is Result.Success -> {
                            binding.progressExportPdf.visibility = View.GONE
                            binding.btnExportPdf.isEnabled = true
                            showShareDialog(state.data.absolutePath)
                            reportsViewModel.resetPdfState()
                        }
                        is Result.Error -> {
                            binding.progressExportPdf.visibility = View.GONE
                            binding.btnExportPdf.isEnabled = true
                            Snackbar.make(binding.root, state.message, Snackbar.LENGTH_LONG).show()
                            reportsViewModel.resetPdfState()
                        }
                        null -> Unit
                    }
                }
            }
        }
    }

    private fun showShareDialog(filePath: String) {
        val file = java.io.File(filePath)
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Reporte Generado")
            .setMessage("¿Deseas compartir el reporte en PDF?")
            .setPositiveButton("Compartir") { _, _ ->
                val intent = reportsViewModel.shareFile(requireContext(), file, "application/pdf")
                startActivity(android.content.Intent.createChooser(intent, "Compartir Reporte"))
            }
            .setNegativeButton("Cerrar", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
