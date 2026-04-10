package com.caas.app.ui.product

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
import com.caas.app.R
import com.caas.app.core.result.Result
import com.caas.app.data.model.Product
import com.caas.app.databinding.FragmentProductListBinding
import com.caas.app.ui.product.adapter.ProductListAdapter
import com.google.android.material.chip.Chip
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch

class ProductListFragment : Fragment() {

    private var _binding: FragmentProductListBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ProductViewModel by activityViewModels()
    private val args: ProductListFragmentArgs by navArgs()
    private lateinit var adapter: ProductListAdapter

    private var allProducts: List<Product> = emptyList()
    private var selectedCategory: String? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProductListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupSearch()
        setupFab()
        observeProductListState()
        observeSearchState()
        observeCategoryFilterState()
    }

    override fun onStart() {
        super.onStart()
        viewModel.getProductsByBusiness(args.businessId)
    }

    private fun setupRecyclerView() {
        adapter = ProductListAdapter { product ->
            findNavController().navigate(
                ProductListFragmentDirections.actionProductListToProductDetail(
                    args.businessId, product.id
                )
            )
        }
        binding.rvProductList.layoutManager = LinearLayoutManager(requireContext())
        binding.rvProductList.adapter = adapter
    }

    private fun setupSearch() {
        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val query = s?.toString() ?: ""
                viewModel.searchProducts(args.businessId, query)
            }
        })
    }

    private fun setupFab() {
        binding.fabAddProduct.setOnClickListener {
            findNavController().navigate(
                ProductListFragmentDirections.actionProductListToCreateProduct(args.businessId)
            )
        }
    }

    private fun buildCategoryChips(products: List<Product>) {
        val categories = products.map { it.category }.distinct().sorted()
        val chipGroup = binding.chipGroupCategories

        // Conservar solo el chip "Todos"
        val chipAll = binding.chipAll
        chipGroup.removeAllViews()
        chipGroup.addView(chipAll)

        categories.forEach { category ->
            val chip = Chip(requireContext()).apply {
                text = category
                isCheckable = true
                isChecked = selectedCategory == category
                setOnCheckedChangeListener { _, isChecked ->
                    if (isChecked) {
                        selectedCategory = category
                        viewModel.getProductsByCategory(args.businessId, category)
                    }
                }
            }
            chipGroup.addView(chip)
        }

        chipAll.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                selectedCategory = null
                showProducts(allProducts)
            }
        }
    }

    private fun observeProductListState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.productListState.collect { state ->
                    when (state) {
                        is Result.Loading -> showLoading(true)
                        is Result.Success -> {
                            showLoading(false)
                            allProducts = state.data
                            buildCategoryChips(allProducts)
                            if (allProducts.isEmpty()) showEmptyState() else showProducts(allProducts)
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

    private fun observeSearchState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.searchState.collect { state ->
                    when (state) {
                        is Result.Loading -> showLoading(true)
                        is Result.Success -> {
                            showLoading(false)
                            if (state.data.isEmpty()) showEmptyState() else showProducts(state.data)
                        }
                        is Result.Error -> {
                            showLoading(false)
                            Snackbar.make(binding.root, state.message, Snackbar.LENGTH_LONG).show()
                        }
                        null -> {}
                    }
                }
            }
        }
    }

    private fun observeCategoryFilterState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.categoryFilterState.collect { state ->
                    when (state) {
                        is Result.Loading -> showLoading(true)
                        is Result.Success -> {
                            showLoading(false)
                            if (state.data.isEmpty()) showEmptyState() else showProducts(state.data)
                        }
                        is Result.Error -> {
                            showLoading(false)
                            Snackbar.make(binding.root, state.message, Snackbar.LENGTH_LONG).show()
                        }
                        null -> {}
                    }
                }
            }
        }
    }

    private fun showLoading(isLoading: Boolean) {
        val progressBar = binding.root.findViewById<View>(R.id.progressBar)
        progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
    }

    private fun showEmptyState() {
        val emptyState = binding.root.findViewById<View>(R.id.tvEmptyState)
        emptyState.visibility = View.VISIBLE
        binding.rvProductList.visibility = View.GONE
    }

    private fun showProducts(products: List<Product>) {
        val emptyState = binding.root.findViewById<View>(R.id.tvEmptyState)
        emptyState.visibility = View.GONE
        binding.rvProductList.visibility = View.VISIBLE
        adapter.submitList(products)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
