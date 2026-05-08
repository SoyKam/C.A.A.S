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
        setupExportButton()
        observeExportStates()
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

    // ── Exportación Excel ────────────────────────────────────────────────────

    private fun setupExportButton() {
        binding.btnExportExcel.setOnClickListener { openExportDialog() }
    }

    private fun openExportDialog() {
        val businessResult = businessViewModel.businessListState.value
        if (businessResult !is Result.Success || businessResult.data.isEmpty()) {
            Snackbar.make(binding.root, "No hay un negocio disponible", Snackbar.LENGTH_SHORT).show()
            return
        }
        val business = businessResult.data.first()
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
        val etDateFrom = dialogView.findViewById<TextInputEditText>(R.id.etDateFrom)
        val etDateTo = dialogView.findViewById<TextInputEditText>(R.id.etDateTo)
        val btnGenerate = dialogView.findViewById<MaterialButton>(R.id.btnGenerate)
        val btnCancel = dialogView.findViewById<MaterialButton>(R.id.btnDialogCancel)

        val displayFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

        // Fechas por defecto: último mes
        val cal = Calendar.getInstance()
        var endDate = cal.timeInMillis
        cal.add(Calendar.MONTH, -1)
        var startDate = cal.timeInMillis

        etDateFrom.setText(displayFormat.format(Date(startDate)))
        etDateTo.setText(displayFormat.format(Date(endDate)))

        etDateFrom.setOnClickListener {
            MaterialDatePicker.Builder.datePicker()
                .setTitleText(getString(R.string.export_date_from))
                .setSelection(startDate)
                .build()
                .also { picker ->
                    picker.addOnPositiveButtonClickListener { selection ->
                        startDate = selection
                        etDateFrom.setText(displayFormat.format(Date(startDate)))
                    }
                    picker.show(childFragmentManager, "picker_from")
                }
        }

        etDateTo.setOnClickListener {
            MaterialDatePicker.Builder.datePicker()
                .setTitleText(getString(R.string.export_date_to))
                .setSelection(endDate)
                .build()
                .also { picker ->
                    picker.addOnPositiveButtonClickListener { selection ->
                        endDate = selection
                        etDateTo.setText(displayFormat.format(Date(endDate)))
                    }
                    picker.show(childFragmentManager, "picker_to")
                }
        }

        val branchNames = branches.map { it.name }
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, branchNames)
        actvBranch.setAdapter(adapter)

        var selectedBranch: Branch? = null
        actvBranch.setOnItemClickListener { _, _, position, _ ->
            selectedBranch = branches[position]
        }

        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setView(dialogView)
            .create()

        btnCancel.setOnClickListener { dialog.dismiss() }
        btnGenerate.setOnClickListener {
            val branch = selectedBranch ?: run {
                Snackbar.make(binding.root, getString(R.string.export_select_branch), Snackbar.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (startDate > endDate) {
                Snackbar.make(binding.root, "La fecha desde no puede ser mayor a la fecha hasta", Snackbar.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            dialog.dismiss()
            reportsViewModel.exportMovementsExcel(
                context = requireContext(),
                businessId = business.id,
                businessName = business.name,
                branchId = branch.id,
                branchName = branch.name,
                startDate = startDate,
                endDate = endDate
            )
        }
        dialog.show()
    }

    private fun observeExportStates() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                reportsViewModel.exportExcelState.collect { state ->
                    when (state) {
                        is Result.Loading -> {
                            binding.progressExportExcel.visibility = View.VISIBLE
                            binding.btnExportExcel.isEnabled = false
                        }
                        is Result.Success -> {
                            binding.progressExportExcel.visibility = View.GONE
                            binding.btnExportExcel.isEnabled = true
                            showShareDialog(state.data.absolutePath, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
                            reportsViewModel.resetExcelState()
                        }
                        is Result.Error -> {
                            binding.progressExportExcel.visibility = View.GONE
                            binding.btnExportExcel.isEnabled = true
                            Snackbar.make(binding.root, state.message, Snackbar.LENGTH_LONG).show()
                            reportsViewModel.resetExcelState()
                        }
                        null -> Unit
                    }
                }
            }
        }
    }

    private fun showShareDialog(filePath: String, mimeType: String) {
        val file = java.io.File(filePath)
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.export_file_ready)
            .setPositiveButton(R.string.export_share) { _, _ ->
                val intent = reportsViewModel.shareFile(requireContext(), file, mimeType)
                startActivity(android.content.Intent.createChooser(intent, getString(R.string.export_share)))
            }
            .setNegativeButton(R.string.export_download_only) { _, _ ->
                Snackbar.make(binding.root, "${getString(R.string.export_success_excel)}: ${file.name}", Snackbar.LENGTH_LONG).show()
            }
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
