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
import com.caas.app.data.model.Provider
import com.caas.app.databinding.FragmentProviderDetailBinding
import com.caas.app.ui.product.ProductViewModel
import com.caas.app.ui.provider.adapter.ProviderProductAdapter
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch

class ProviderDetailFragment : Fragment() {

    private var _binding: FragmentProviderDetailBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ProviderViewModel by activityViewModels()
    private val productViewModel: ProductViewModel by activityViewModels()
    private val args: ProviderDetailFragmentArgs by navArgs()

    private lateinit var productAdapter: ProviderProductAdapter
    private var currentProvider: Provider? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProviderDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupProductRecyclerView()
        setupClickListeners()
        observeProviderState()
        observeProductListState()
        observeRemoveProductState()
    }

    override fun onStart() {
        super.onStart()
        viewModel.getProviderById(args.businessId, args.providerId)
        productViewModel.getProductsByBusiness(args.businessId)
    }

    private fun setupProductRecyclerView() {
        productAdapter = ProviderProductAdapter { productId ->
            viewModel.removeProduct(args.businessId, args.providerId, productId)
        }
        binding.rvProducts.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        binding.rvProducts.adapter = productAdapter
    }

    private fun setupClickListeners() {
        binding.btnBack.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.btnEdit.setOnClickListener {
            if (findNavController().currentDestination?.id ==
                com.caas.app.R.id.providerDetailFragment
            ) {
                findNavController().navigate(
                    ProviderDetailFragmentDirections.actionProviderDetailToEditProvider(
                        args.businessId, args.providerId
                    )
                )
            }
        }

        binding.btnAssociateProduct.setOnClickListener {
            if (findNavController().currentDestination?.id ==
                com.caas.app.R.id.providerDetailFragment
            ) {
                findNavController().navigate(
                    ProviderDetailFragmentDirections.actionProviderDetailToAssociateProduct(
                        args.businessId, args.providerId
                    )
                )
            }
        }
    }

    private fun observeProviderState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.providerState.collect { state ->
                    when (state) {
                        is Result.Loading -> showLoading(true)
                        is Result.Success -> {
                            showLoading(false)
                            state.data?.let { displayProvider(it) }
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

    private fun observeProductListState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                productViewModel.productListState.collect { state ->
                    if (state is Result.Success) {
                        val provider = currentProvider ?: return@collect
                        val productMap = state.data.associateBy { it.id }
                        val chips = provider.productIds.mapNotNull { id ->
                            productMap[id]?.let { product -> id to product.name }
                        }
                        updateProductChips(chips)
                    }
                }
            }
        }
    }

    private fun observeRemoveProductState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.removeProductState.collect { state ->
                    when (state) {
                        is Result.Success -> {
                            viewModel.resetRemoveProductState()
                            viewModel.getProviderById(args.businessId, args.providerId)
                        }
                        is Result.Error -> {
                            Snackbar.make(binding.root, state.message, Snackbar.LENGTH_LONG).show()
                            viewModel.resetRemoveProductState()
                        }
                        else -> {}
                    }
                }
            }
        }
    }

    private fun displayProvider(provider: Provider) {
        currentProvider = provider
        binding.tvName.text = provider.name
        binding.tvPhone.text = provider.phone
        binding.tvEmail.text = provider.email

        val productListState = productViewModel.productListState.value
        if (productListState is Result.Success) {
            val productMap = productListState.data.associateBy { it.id }
            val chips = provider.productIds.mapNotNull { id ->
                productMap[id]?.let { product -> id to product.name }
            }
            updateProductChips(chips)
        }
    }

    private fun updateProductChips(chips: List<Pair<String, String>>) {
        productAdapter.submitList(chips)
        if (chips.isEmpty()) {
            binding.tvNoProducts.visibility = View.VISIBLE
            binding.rvProducts.visibility = View.GONE
        } else {
            binding.tvNoProducts.visibility = View.GONE
            binding.rvProducts.visibility = View.VISIBLE
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
