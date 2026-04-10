package com.caas.app.ui.provider

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
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
import com.caas.app.data.model.Product
import com.caas.app.databinding.FragmentAssociateProductBinding
import com.caas.app.ui.product.ProductViewModel
import com.caas.app.ui.provider.adapter.ProductSelectableAdapter
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch

class AssociateProductFragment : Fragment() {

    private var _binding: FragmentAssociateProductBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ProviderViewModel by activityViewModels()
    private val productViewModel: ProductViewModel by activityViewModels()
    private val args: AssociateProductFragmentArgs by navArgs()

    private lateinit var adapter: ProductSelectableAdapter
    private var allProducts: List<Product> = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAssociateProductBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupSearch()
        setupConfirmButton()
        observeProductListState()
        observeProviderState()
        observeAssociateState()
        productViewModel.getProductsByBusiness(args.businessId)
        viewModel.getProviderById(args.businessId, args.providerId)
    }

    private fun setupRecyclerView() {
        adapter = ProductSelectableAdapter()
        binding.rvProducts.layoutManager = LinearLayoutManager(requireContext())
        binding.rvProducts.adapter = adapter
    }

    private fun setupSearch() {
        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val query = s?.toString()?.trim() ?: ""
                val filtered = if (query.isBlank()) allProducts
                else allProducts.filter { it.name.contains(query, ignoreCase = true) }
                adapter.submitList(filtered)
            }
        })
    }

    private fun setupConfirmButton() {
        binding.btnConfirm.setOnClickListener {
            val selectedIds = adapter.getSelectedIds()
            if (selectedIds.isEmpty()) {
                findNavController().navigateUp()
                return@setOnClickListener
            }
            selectedIds.forEach { productId ->
                viewModel.associateProduct(args.businessId, args.providerId, productId)
            }
        }
    }

    private fun observeProductListState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                productViewModel.productListState.collect { state ->
                    when (state) {
                        is Result.Loading -> showLoading(true)
                        is Result.Success -> {
                            showLoading(false)
                            allProducts = state.data
                            adapter.submitList(allProducts)
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

    private fun observeProviderState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.providerState.collect { state ->
                    if (state is Result.Success) {
                        state.data?.let { adapter.setAlreadyAssociated(it.productIds) }
                    }
                }
            }
        }
    }

    private fun observeAssociateState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.associateProductState.collect { state ->
                    when (state) {
                        is Result.Success -> {
                            viewModel.resetAssociateState()
                            findNavController().navigateUp()
                        }
                        is Result.Error -> {
                            Snackbar.make(binding.root, state.message, Snackbar.LENGTH_LONG).show()
                            viewModel.resetAssociateState()
                        }
                        else -> {}
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
