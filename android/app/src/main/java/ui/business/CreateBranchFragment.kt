package com.caas.app.ui.business

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
import android.util.Log
import com.caas.app.core.result.Result
import com.caas.app.databinding.FragmentCreateBranchBinding
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch

class CreateBranchFragment : Fragment() {

    private var _binding: FragmentCreateBranchBinding? = null
    private val binding get() = _binding!!

    private val viewModel: BranchViewModel by activityViewModels()
    private val args: CreateBranchFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCreateBranchBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        android.widget.Toast.makeText(
            requireContext(),
            "ID: '${args.businessId}' (len=${args.businessId.length})",
            android.widget.Toast.LENGTH_LONG
        ).show()
        setupClickListeners()
        observeCreateBranchState()
    }

    private fun setupClickListeners() {
        binding.btnCreateBranch.setOnClickListener {
            val name = binding.etBranchName.text.toString().trim()
            val address = binding.etBranchAddress.text.toString().trim()
            val phone = binding.etBranchPhone.text.toString().trim()
            Log.d("BRANCH_DEBUG", "button clicked — businessId='${args.businessId}' name='$name'")

            if (name.isEmpty() || address.isEmpty() || phone.isEmpty()) {
                Snackbar.make(binding.root, "Todos los campos son requeridos", Snackbar.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            viewModel.createBranch(args.businessId, name, address, phone)
        }
    }

    private fun observeCreateBranchState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.createBranchState.collect { state ->
                    Log.d("BRANCH_DEBUG", "Fragment collected state: $state")
                    when (state) {
                        is Result.Loading -> showLoading(true)
                        is Result.Success -> {
                            showLoading(false)
                            viewModel.resetCreateState()
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
        binding.btnCreateBranch.isEnabled = !isLoading
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
