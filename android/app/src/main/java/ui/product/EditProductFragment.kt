package com.caas.app.ui.product

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.caas.app.core.result.Result
import com.caas.app.data.model.Product
import com.caas.app.databinding.FragmentEditProductBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch

class EditProductFragment : Fragment() {

    private var _binding: FragmentEditProductBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ProductViewModel by activityViewModels()
    private val args: EditProductFragmentArgs by navArgs()

    private var selectedImageUri: Uri? = null
    private var currentImageUrl: String = ""

    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            selectedImageUri = result.data?.data
            selectedImageUri?.let {
                binding.ivImagePreview.setImageURI(it)
                binding.ivImagePreview.visibility = View.VISIBLE
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEditProductBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupClickListeners()
        observeProductState()
        observeUpdateState()
        observeDeleteState()
        viewModel.getProductById(args.businessId, args.productId)
    }

    private fun setupClickListeners() {
        binding.btnBack.setOnClickListener { findNavController().navigateUp() }

        binding.btnSelectImage.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK).apply { type = "image/*" }
            pickImageLauncher.launch(intent)
        }

        binding.btnSaveProduct.setOnClickListener {
            val name = binding.etName.text?.toString()?.trim() ?: ""
            val sku = binding.etSku.text?.toString()?.trim() ?: ""
            val category = binding.etCategory.text?.toString()?.trim() ?: ""
            val costPriceText = binding.etCostPrice.text?.toString()?.trim() ?: ""
            val salePriceText = binding.etSalePrice.text?.toString()?.trim() ?: ""

            val costPrice = costPriceText.toDoubleOrNull() ?: 0.0
            val salePrice = salePriceText.toDoubleOrNull() ?: 0.0
            val imageUrl = selectedImageUri?.toString() ?: currentImageUrl

            viewModel.updateProduct(args.businessId, args.productId, name, sku, category, costPrice, salePrice, imageUrl)
        }

        binding.btnDeleteProduct.setOnClickListener {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("Eliminar producto")
                .setMessage("¿Estás seguro de que deseas eliminar este producto? Esta acción no se puede deshacer.")
                .setNegativeButton("Cancelar", null)
                .setPositiveButton("Eliminar") { _, _ ->
                    viewModel.deleteProduct(args.businessId, args.productId)
                }
                .show()
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
                            state.data?.let { prefillFields(it) }
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

    private fun observeUpdateState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.updateProductState.collect { state ->
                    when (state) {
                        is Result.Loading -> showLoading(true)
                        is Result.Success -> {
                            showLoading(false)
                            viewModel.resetUpdateState()
                            viewModel.getProductsByBusiness(args.businessId)
                            findNavController().navigateUp()
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

    private fun observeDeleteState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.deleteProductState.collect { state ->
                    when (state) {
                        is Result.Loading -> showLoading(true)
                        is Result.Success -> {
                            showLoading(false)
                            viewModel.resetDeleteState()
                            viewModel.getProductsByBusiness(args.businessId)
                            findNavController().navigate(
                                EditProductFragmentDirections.actionEditProductToProductList(args.businessId)
                            )
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

    private fun prefillFields(product: Product) {
        binding.etName.setText(product.name)
        binding.etSku.setText(product.sku)
        binding.etCategory.setText(product.category)
        binding.etCostPrice.setText(product.costPrice.toString())
        binding.etSalePrice.setText(product.salePrice.toString())
        currentImageUrl = product.imageUrl
        if (product.imageUrl.isNotBlank()) {
            binding.ivImagePreview.visibility = View.VISIBLE
        }
    }

    private fun showLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.btnSaveProduct.isEnabled = !isLoading
        binding.btnDeleteProduct.isEnabled = !isLoading
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
