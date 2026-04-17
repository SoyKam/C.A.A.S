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
import com.caas.app.data.model.MovementType
import com.caas.app.data.model.StockMovement
import com.caas.app.databinding.FragmentStockMovementsBinding
import com.caas.app.ui.stock.adapter.StockMovementsAdapter
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch
import java.util.Calendar

class StockMovementsFragment : Fragment() {

    private var _binding: FragmentStockMovementsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: StockViewModel by activityViewModels()
    private val args: StockMovementsFragmentArgs by navArgs()
    private lateinit var adapter: StockMovementsAdapter

    private enum class ActiveFilter { ALL, TYPE, DATE }
    private var activeFilter = ActiveFilter.ALL
    private var isUpdatingChips = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStockMovementsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupClickListeners()
        setupChipFilters()
        observeStates()
    }

    override fun onStart() {
        super.onStart()
        viewModel.getMovements(args.businessId, args.branchId)
    }

    private fun setupRecyclerView() {
        adapter = StockMovementsAdapter { movement ->
            viewModel.selectMovement(movement)
            findNavController().navigate(
                StockMovementsFragmentDirections.actionStockMovementsToMovementDetail(
                    args.businessId, args.branchId, movement.id
                )
            )
        }
        binding.rvMovements.layoutManager = LinearLayoutManager(requireContext())
        binding.rvMovements.adapter = adapter
    }

    private fun setupClickListeners() {
        binding.btnBack.setOnClickListener { findNavController().navigateUp() }
    }

    private fun setupChipFilters() {
        binding.chipGroupType.setOnCheckedStateChangeListener { _, checkedIds ->
            if (isUpdatingChips || checkedIds.isEmpty()) return@setOnCheckedStateChangeListener
            isUpdatingChips = true
            binding.chipGroupDate.clearCheck()
            isUpdatingChips = false

            activeFilter = if (checkedIds.first() == binding.chipAll.id) ActiveFilter.ALL else ActiveFilter.TYPE
            when (checkedIds.first()) {
                binding.chipAll.id -> viewModel.getMovements(args.businessId, args.branchId)
                binding.chipEntry.id -> viewModel.getMovementsByType(args.businessId, args.branchId, MovementType.ENTRY)
                binding.chipSale.id -> viewModel.getMovementsByType(args.businessId, args.branchId, MovementType.SALE)
                binding.chipDamage.id -> viewModel.getMovementsByType(args.businessId, args.branchId, MovementType.DAMAGE)
                binding.chipTransfer.id -> viewModel.getMovementsByType(args.businessId, args.branchId, MovementType.TRANSFER)
            }
        }

        binding.chipGroupDate.setOnCheckedStateChangeListener { _, checkedIds ->
            if (isUpdatingChips || checkedIds.isEmpty()) return@setOnCheckedStateChangeListener
            isUpdatingChips = true
            binding.chipAll.isChecked = true
            isUpdatingChips = false

            activeFilter = ActiveFilter.DATE
            val (start, end) = dateRange(checkedIds.first())
            viewModel.getMovementsByDateRange(args.businessId, args.branchId, start, end)
        }
    }

    private fun dateRange(chipId: Int): Pair<Long, Long> {
        val now = System.currentTimeMillis()
        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return when (chipId) {
            binding.chipToday.id -> Pair(cal.timeInMillis, now)
            binding.chipWeek.id -> {
                cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
                Pair(cal.timeInMillis, now)
            }
            binding.chipMonth.id -> {
                cal.set(Calendar.DAY_OF_MONTH, 1)
                Pair(cal.timeInMillis, now)
            }
            else -> Pair(0L, now)
        }
    }

    private fun observeStates() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.movementsState.collect { state ->
                        if (activeFilter == ActiveFilter.ALL) handleResult(state)
                    }
                }
                launch {
                    viewModel.movementsByTypeState.collect { state ->
                        if (activeFilter == ActiveFilter.TYPE) handleResult(state)
                    }
                }
                launch {
                    viewModel.movementsByDateState.collect { state ->
                        if (activeFilter == ActiveFilter.DATE) handleResult(state)
                    }
                }
            }
        }
    }

    private fun handleResult(state: Result<List<StockMovement>>?) {
        when (state) {
            is Result.Loading -> showLoading(true)
            is Result.Success -> {
                showLoading(false)
                if (state.data.isEmpty()) showEmptyState() else showMovements(state.data)
            }
            is Result.Error -> {
                showLoading(false)
                Snackbar.make(binding.root, state.message, Snackbar.LENGTH_LONG).show()
            }
            null -> Unit
        }
    }

    private fun showLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
    }

    private fun showEmptyState() {
        binding.tvEmptyState.visibility = View.VISIBLE
        binding.rvMovements.visibility = View.GONE
    }

    private fun showMovements(movements: List<StockMovement>) {
        binding.tvEmptyState.visibility = View.GONE
        binding.rvMovements.visibility = View.VISIBLE
        adapter.submitList(movements)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
