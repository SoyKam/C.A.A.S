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
import com.caas.app.databinding.FragmentCreateProductBinding
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch

class CreateProductFragment : Fragment() {

    private var _binding: FragmentCreateProductBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ProductViewModel by activityViewModels()
    private val args: CreateProductFragmentArgs by navArgs()

    private var selectedImageUri: Uri? = null

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
        _binding = FragmentCreateProductBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupClickListeners()
        observeCreateState()
    }

    private fun setupClickListeners() {
        binding.btnSelectImage.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK).apply { type = "image/*" }
            pickImageLauncher.launch(intent)
        }

        binding.btnCreateProduct.setOnClickListener {
            val name = binding.etName.text?.toString()?.trim() ?: ""
            val sku = binding.etSku.text?.toString()?.trim() ?: ""
            val category = binding.etCategory.text?.toString()?.trim() ?: ""
            val costPriceText = binding.etCostPrice.text?.toString()?.trim() ?: ""
            val salePriceText = binding.etSalePrice.text?.toString()?.trim() ?: ""

            val costPrice = costPriceText.toDoubleOrNull() ?: 0.0
            val salePrice = salePriceText.toDoubleOrNull() ?: 0.0
            val imageUrl = selectedImageUri?.toString() ?: ""

            viewModel.createProduct(args.businessId, name, sku, category, costPrice, salePrice, imageUrl)
        }
    }

    private fun observeCreateState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.createProductState.collect { state ->
                    when (state) {
                        is Result.Loading -> showLoading(true)
                        is Result.Success -> {
                            showLoading(false)
                            viewModel.resetCreateState()
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

    private fun showLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.btnCreateProduct.isEnabled = !isLoading
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
