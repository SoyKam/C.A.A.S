package com.caas.app.ui.reports

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.caas.app.R
import com.caas.app.core.result.Result
import com.caas.app.data.model.Branch
import com.caas.app.data.model.Business
import com.caas.app.data.model.StockMovement
import com.caas.app.databinding.FragmentReportsBinding
import com.caas.app.ui.business.BusinessViewModel
import com.caas.app.ui.stock.StockViewModel
import com.caas.app.ui.stock.adapter.StockMovementsAdapter
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class ReportsFragment : Fragment() {

    private var _binding: FragmentReportsBinding? = null
    private val binding get() = _binding!!

    private val businessViewModel: BusinessViewModel by activityViewModels()
    private val stockViewModel: StockViewModel by activityViewModels()
    private val reportsViewModel: ReportsViewModel by activityViewModels()
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
        setupExportButtons()
        observeExportStates()
    }

    override fun onStart() {
        super.onStart()
        val current = businessViewModel.businessListState.value
        if (current is Result.Success && current.data.isNotEmpty()) {
            stockViewModel.loadRecentMovements(current.data.map { it.id }, limit = 20)
        } else {
            businessViewModel.getBusinessesByOwner()
        }
    }

    private fun setupRecyclerView() {
        adapter = StockMovementsAdapter { /* no detail navigation */ }
        binding.rvRecentMovements.layoutManager = LinearLayoutManager(requireContext())
        binding.rvRecentMovements.adapter = adapter
    }

    private fun observeStates() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    businessViewModel.businessListState.collect { state ->
                        if (state is Result.Success && state.data.isNotEmpty() &&
                            stockViewModel.recentMovementsState.value == null) {
                            stockViewModel.loadRecentMovements(state.data.map { it.id }, limit = 20)
                        }
                    }
                }
                launch {
                    stockViewModel.recentMovementsState.collect { state ->
                        when (state) {
                            is Result.Loading -> showLoading(true)
                            is Result.Success -> {
                                showLoading(false)
                                if (state.data.isEmpty()) showEmptyState(true)
                                else {
                                    showEmptyState(false)
                                    adapter.submitList(state.data)
                                }
                            }
                            is Result.Error -> {
                                showLoading(false)
                                showEmptyState(true)
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

    private fun showEmptyState(show: Boolean) {
        binding.layoutEmptyState.visibility = if (show) View.VISIBLE else View.GONE
        binding.rvRecentMovements.visibility = if (show) View.GONE else View.VISIBLE
    }

    private fun setupExportButtons() {
        binding.cardExportPdf.setOnClickListener { openExportDialog(ExportType.PDF) }
        binding.cardExportExcel.setOnClickListener { openExportDialog(ExportType.EXCEL) }
    }

    private fun openExportDialog(type: ExportType) {
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
            showExportOptionsDialog(business, branches, type)
        }
    }

    private fun showExportOptionsDialog(business: Business, branches: List<Branch>, type: ExportType) {
        val dialogView = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_export_options, null)

        val actvBranch = dialogView.findViewById<com.google.android.material.textfield.MaterialAutoCompleteTextView>(R.id.actvBranch)
        val etDateFrom = dialogView.findViewById<TextInputEditText>(R.id.etDateFrom)
        val etDateTo = dialogView.findViewById<TextInputEditText>(R.id.etDateTo)
        val btnGenerate = dialogView.findViewById<MaterialButton>(R.id.btnGenerate)
        val btnCancel = dialogView.findViewById<MaterialButton>(R.id.btnDialogCancel)
        val tilDateFrom = dialogView.findViewById<com.google.android.material.textfield.TextInputLayout>(R.id.tilDateFrom)
        val tilDateTo = dialogView.findViewById<com.google.android.material.textfield.TextInputLayout>(R.id.tilDateTo)

        if (type == ExportType.PDF) {
            tilDateFrom.visibility = View.GONE
            tilDateTo.visibility = View.GONE
        }

        val displayFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val cal = Calendar.getInstance()
        var endDate = cal.timeInMillis
        cal.add(Calendar.MONTH, -1)
        var startDate = cal.timeInMillis

        etDateFrom.setText(displayFormat.format(Date(startDate)))
        etDateTo.setText(displayFormat.format(Date(endDate)))

        etDateFrom.setOnClickListener {
            MaterialDatePicker.Builder.datePicker().setSelection(startDate).build().also { picker ->
                picker.addOnPositiveButtonClickListener { selection ->
                    startDate = selection
                    etDateFrom.setText(displayFormat.format(Date(startDate)))
                }
                picker.show(childFragmentManager, "picker_from")
            }
        }

        etDateTo.setOnClickListener {
            MaterialDatePicker.Builder.datePicker().setSelection(endDate).build().also { picker ->
                picker.addOnPositiveButtonClickListener { selection ->
                    endDate = selection
                    etDateTo.setText(displayFormat.format(Date(endDate)))
                }
                picker.show(childFragmentManager, "picker_to")
            }
        }

        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, branches.map { it.name })
        actvBranch.setAdapter(adapter)

        var selectedBranch: Branch? = null
        actvBranch.setOnItemClickListener { _, _, position, _ -> selectedBranch = branches[position] }

        val dialog = MaterialAlertDialogBuilder(requireContext()).setView(dialogView).create()
        btnCancel.setOnClickListener { dialog.dismiss() }
        btnGenerate.setOnClickListener {
            val branch = selectedBranch ?: run {
                Snackbar.make(binding.root, "Selecciona una sucursal", Snackbar.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            dialog.dismiss()
            if (type == ExportType.PDF) {
                reportsViewModel.exportInventoryPdf(requireContext(), business.id, business.name, branch.id, branch.name)
            } else {
                reportsViewModel.exportMovementsExcel(requireContext(), business.id, business.name, branch.id, branch.name, startDate, endDate)
            }
        }
        dialog.show()
    }

    private fun observeExportStates() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    reportsViewModel.exportPdfState.collect { state ->
                        handleExportResult(state, binding.progressExportPdf, "application/pdf")
                    }
                }
                launch {
                    reportsViewModel.exportExcelState.collect { state ->
                        handleExportResult(state, binding.progressExportExcel, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
                    }
                }
            }
        }
    }

    private fun handleExportResult(state: Result<java.io.File>?, progress: View, mimeType: String) {
        when (state) {
            is Result.Loading -> progress.visibility = View.VISIBLE
            is Result.Success -> {
                progress.visibility = View.GONE
                showShareDialog(state.data.absolutePath, mimeType)
                if (mimeType.contains("pdf")) reportsViewModel.resetPdfState()
                else reportsViewModel.resetExcelState()
            }
            is Result.Error -> {
                progress.visibility = View.GONE
                Snackbar.make(binding.root, state.message, Snackbar.LENGTH_LONG).show()
                if (mimeType.contains("pdf")) reportsViewModel.resetPdfState()
                else reportsViewModel.resetExcelState()
            }
            null -> Unit
        }
    }

    private fun showShareDialog(filePath: String, mimeType: String) {
        val file = java.io.File(filePath)
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Archivo Listo")
            .setMessage("El reporte ha sido generado correctamente. ¿Qué deseas hacer?")
            .setPositiveButton("Compartir") { _, _ ->
                val intent = reportsViewModel.shareFile(requireContext(), file, mimeType)
                startActivity(android.content.Intent.createChooser(intent, "Compartir reporte"))
            }
            .setNegativeButton("Cerrar", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
