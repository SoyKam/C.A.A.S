package com.caas.app.ui.provider

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
import com.caas.app.databinding.FragmentProviderListBinding
import com.caas.app.ui.provider.adapter.ProviderListAdapter
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch

class ProviderListFragment : Fragment() {

    private var _binding: FragmentProviderListBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ProviderViewModel by activityViewModels()
    private val args: ProviderListFragmentArgs by navArgs()
    private lateinit var adapter: ProviderListAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProviderListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupFab()
        observeProviderListState()
    }

    override fun onStart() {
        super.onStart()
        viewModel.getProvidersByBusiness(args.businessId)
    }

    private fun setupRecyclerView() {
        adapter = ProviderListAdapter { provider ->
            findNavController().navigate(
                ProviderListFragmentDirections.actionProviderListToProviderDetail(
                    args.businessId, provider.id
                )
            )
        }
        binding.rvProviderList.layoutManager = LinearLayoutManager(requireContext())
        binding.rvProviderList.adapter = adapter
    }

    private fun setupFab() {
        binding.fabAddProvider.setOnClickListener {
            findNavController().navigate(
                ProviderListFragmentDirections.actionProviderListToCreateProvider(args.businessId)
            )
        }
    }

    private fun observeProviderListState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.providerListState.collect { state ->
                    when (state) {
                        is Result.Loading -> showLoading(true)
                        is Result.Success -> {
                            showLoading(false)
                            if (state.data.isEmpty()) {
                                showEmptyState()
                            } else {
                                adapter.submitList(state.data)
                                showList()
                            }
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
        binding.rvProviderList.visibility = View.GONE
    }

    private fun showList() {
        binding.tvEmptyState.visibility = View.GONE
        binding.rvProviderList.visibility = View.VISIBLE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
