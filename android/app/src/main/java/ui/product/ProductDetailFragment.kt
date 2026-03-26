package com.caas.app.ui.product

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
import com.caas.app.core.result.Result
import com.caas.app.data.model.Product
import com.caas.app.databinding.FragmentProductDetailBinding
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch

class ProductDetailFragment : Fragment() {

    private var _binding: FragmentProductDetailBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ProductViewModel by activityViewModels()
    private val args: ProductDetailFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProductDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupClickListeners()
        observeProductState()
        viewModel.getProductById(args.businessId, args.productId)
    }

    private fun setupClickListeners() {
        binding.btnEdit.setOnClickListener {
            findNavController().navigate(
                ProductDetailFragmentDirections.actionProductDetailToEditProduct(
                    args.businessId, args.productId
                )
            )
        }

        binding.btnBack.setOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun observeProductState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.productState.collect { state ->
                    when (state) {
                        is Result.Loading -> showLoading(true)
                        is Result.Success -> {
                            showLoading(false)
                            state.data?.let { displayProduct(it) }
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

    private fun displayProduct(product: Product) {
        binding.tvProductName.text = product.name
        binding.tvProductSku.text = product.sku
        binding.tvProductCategory.text = product.category
        binding.tvCostPrice.text = getString(com.caas.app.R.string.price_currency_format, product.costPrice)
        binding.tvSalePrice.text = getString(com.caas.app.R.string.price_currency_format, product.salePrice)

        if (product.imageUrl.isNotBlank()) {
            binding.ivProductImage.visibility = View.VISIBLE
        }
    }

    private fun showLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.btnEdit.isEnabled = !isLoading
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
