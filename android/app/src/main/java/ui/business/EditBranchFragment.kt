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
import com.caas.app.core.result.Result
import com.caas.app.data.model.Branch
import com.caas.app.databinding.FragmentEditBranchBinding
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch

class EditBranchFragment : Fragment() {

    private var _binding: FragmentEditBranchBinding? = null
    private val binding get() = _binding!!

    private val viewModel: BranchViewModel by activityViewModels()
    private val args: EditBranchFragmentArgs by navArgs()

    private var currentBranch: Branch? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEditBranchBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupClickListeners()
        observeBranchState()
        observeUpdateState()
        observeDeleteState()
        viewModel.getBranchById(args.businessId, args.branchId)
    }

    private fun setupClickListeners() {
        binding.btnSaveBranch.setOnClickListener {
            val branch = currentBranch ?: return@setOnClickListener
            val name = binding.etBranchName.text.toString().trim()
            val address = binding.etBranchAddress.text.toString().trim()
            val phone = binding.etBranchPhone.text.toString().trim()

            if (name.isEmpty() || address.isEmpty() || phone.isEmpty()) {
                Snackbar.make(binding.root, "Todos los campos son requeridos", Snackbar.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            viewModel.updateBranch(branch.id, branch.businessId, name, address, phone, branch.createdAt)
        }

        binding.btnDeleteBranch.setOnClickListener {
            viewModel.deleteBranch(args.businessId, args.branchId)
        }
    }

    private fun observeBranchState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.branchState.collect { state ->
                    when (state) {
                        is Result.Loading -> showLoading(true)
                        is Result.Success -> {
                            showLoading(false)
                            currentBranch = state.data
                            binding.etBranchName.setText(state.data.name)
                            binding.etBranchAddress.setText(state.data.address)
                            binding.etBranchPhone.setText(state.data.phone)
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
                viewModel.updateBranchState.collect { state ->
                    when (state) {
                        is Result.Loading -> showLoading(true)
                        is Result.Success -> {
                            showLoading(false)
                            viewModel.resetUpdateState()
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
                viewModel.deleteBranchState.collect { state ->
                    when (state) {
                        is Result.Loading -> showLoading(true)
                        is Result.Success -> {
                            showLoading(false)
                            viewModel.resetDeleteState()
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
        binding.btnSaveBranch.isEnabled = !isLoading
        binding.btnDeleteBranch.isEnabled = !isLoading
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
