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
import com.caas.app.R
import com.caas.app.core.result.Result
import com.caas.app.data.model.Branch
import com.caas.app.data.model.Business
import com.caas.app.data.model.MovementType
import com.caas.app.data.model.StockMovement
import com.caas.app.databinding.FragmentChartsBinding
import com.caas.app.ui.business.BusinessViewModel
import com.caas.app.ui.stock.StockViewModel
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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
        observeData()
        setupExportButton()
        observeExportStates()
    }

    override fun onStart() {
        super.onStart()
        val current = businessViewModel.businessListState.value
        if (current is Result.Success && current.data.isNotEmpty()) {
            stockViewModel.loadRecentMovements(current.data.map { it.id })
        } else {
            businessViewModel.getBusinessesByOwner()
        }
    }

    private fun observeData() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    businessViewModel.businessListState.collect { state ->
                        if (state is Result.Success && state.data.isNotEmpty() &&
                            stockViewModel.recentMovementsState.value == null) {
                            stockViewModel.loadRecentMovements(state.data.map { it.id })
                        }
                    }
                }

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
            if (count > 0 && lp.weight < 0.05f) lp.weight = 0.05f
            view.layoutParams = lp
        }
    }

    // ── Exportación PDF ──────────────────────────────────────────────────────

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

        // PDF no necesita rango de fechas
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
            .create()

        btnCancel.setOnClickListener { dialog.dismiss() }
        btnGenerate.setOnClickListener {
            val branch = selectedBranch ?: run {
                Snackbar.make(binding.root, getString(R.string.export_select_branch), Snackbar.LENGTH_SHORT).show()
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
                            showShareDialog(state.data.absolutePath, "application/pdf")
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

    private fun showShareDialog(filePath: String, mimeType: String) {
        val file = java.io.File(filePath)
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.export_file_ready)
            .setPositiveButton(R.string.export_share) { _, _ ->
                val intent = reportsViewModel.shareFile(requireContext(), file, mimeType)
                startActivity(android.content.Intent.createChooser(intent, getString(R.string.export_share)))
            }
            .setNegativeButton(R.string.export_download_only) { _, _ ->
                Snackbar.make(binding.root, "${getString(R.string.export_success_pdf)}: ${file.name}", Snackbar.LENGTH_LONG).show()
            }
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
