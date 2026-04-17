package com.caas.app.ui.home

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
import androidx.recyclerview.widget.LinearLayoutManager
import com.caas.app.R
import com.caas.app.core.result.Result
import com.caas.app.data.model.Business
import com.caas.app.databinding.FragmentHomeBinding
import com.caas.app.ui.business.BusinessViewModel
import com.caas.app.ui.business.adapter.BusinessListAdapter
import com.caas.app.ui.stock.StockViewModel
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val viewModel: BusinessViewModel by activityViewModels()
    private val stockViewModel: StockViewModel by activityViewModels()
    private lateinit var previewAdapter: BusinessListAdapter
    private var alertBusinessId: String = ""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUserInfo()
        setupRecyclerView()
        setupClickListeners()
        observeBusinessListState()
        observeAlertsState()
        viewModel.getBusinessesByOwner()
    }

    private fun setupUserInfo() {
        val displayName = FirebaseAuth.getInstance().currentUser?.displayName
        val firstName = displayName?.split(" ")?.firstOrNull() ?: "Usuario"
        binding.tvWelcomeUser.text = "Hola, $firstName"
    }

    private fun setupRecyclerView() {
        previewAdapter = BusinessListAdapter { businessId ->
            findNavController().navigate(
                HomeFragmentDirections.actionHomeToBusinessDetail(businessId)
            )
        }
        binding.rvBusinessPreview.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = previewAdapter
            isNestedScrollingEnabled = false
        }
    }

    private fun setupClickListeners() {
        binding.btnSeeAll.setOnClickListener {
            requireActivity().findViewById<BottomNavigationView>(R.id.bottomNavigation)
                .selectedItemId = R.id.businessListFragment
        }
        binding.fabCreateBusiness.setOnClickListener {
            findNavController().navigate(
                HomeFragmentDirections.actionHomeToCreateBusiness()
            )
        }
        binding.btnCreateFirstBusiness.setOnClickListener {
            findNavController().navigate(
                HomeFragmentDirections.actionHomeToCreateBusiness()
            )
        }
        binding.alertBanner.setOnClickListener {
            if (alertBusinessId.isNotEmpty()) {
                findNavController().navigate(
                    HomeFragmentDirections.actionHomeToLowStockSummary(alertBusinessId)
                )
            }
        }
    }

    private fun observeBusinessListState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.businessListState.collect { state ->
                    when (state) {
                        is Result.Loading -> showLoading(true)
                        is Result.Success -> {
                            showLoading(false)
                            updateUI(state.data)
                        }
                        is Result.Error -> {
                            showLoading(false)
                            updateUI(emptyList())
                        }
                        null -> Unit
                    }
                }
            }
        }
    }

    private fun updateUI(businesses: List<Business>) {
        val total = businesses.size
        val active = businesses.count { it.isActive }
        val inactive = businesses.count { !it.isActive }

        binding.tvStatTotal.text = total.toString()
        binding.tvStatActive.text = active.toString()
        binding.tvStatPending.text = inactive.toString()

        if (businesses.isNotEmpty()) {
            alertBusinessId = businesses.first().id
            stockViewModel.getUnreadAlerts(alertBusinessId)
        } else {
            binding.alertBanner.visibility = View.GONE
        }

        if (businesses.isEmpty()) {
            binding.rvBusinessPreview.visibility = View.GONE
            binding.layoutEmptyState.visibility = View.VISIBLE
            binding.btnSeeAll.visibility = View.GONE
        } else {
            binding.rvBusinessPreview.visibility = View.VISIBLE
            binding.layoutEmptyState.visibility = View.GONE
            binding.btnSeeAll.visibility = if (businesses.size > 3) View.VISIBLE else View.GONE
            previewAdapter.submitList(businesses.take(3))
        }
    }

    private fun observeAlertsState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                stockViewModel.unreadAlertsState.collect { state ->
                    when (state) {
                        is Result.Success -> {
                            val count = state.data.size
                            if (count > 0) {
                                binding.alertBanner.visibility = View.VISIBLE
                                binding.tvAlertTitle.text = "¡Hay $count alerta${if (count > 1) "s" else ""} de stock!"
                                binding.tvAlertSubtitle.text = "Productos por debajo del stock mínimo"
                            } else {
                                binding.alertBanner.visibility = View.GONE
                            }
                        }
                        is Result.Error -> binding.alertBanner.visibility = View.GONE
                        else -> Unit
                    }
                }
            }
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
