package com.caas.app.ui.auth.login

import android.content.Intent
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
import com.caas.app.R
import com.caas.app.core.utils.Resource
import com.caas.app.databinding.FragmentLoginBinding
import com.caas.app.ui.auth.AuthViewModel
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch
import com.caas.app.ui.home.HomeActivity

/**
 * Fragmento de inicio de sesión.
 * Captura email/password, valida y observa el resultado del ViewModel.
 */
class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    private val viewModel: AuthViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupClickListeners()
        observeLoginState()
    }

    /**
     * Configura los listeners de botones.
     */
    private fun setupClickListeners() {
        // Botón de login
        binding.loginButton.setOnClickListener {
            val email = binding.emailEditText.text.toString().trim()
            val password = binding.passwordEditText.text.toString()
            viewModel.login(email, password)
        }

        // Navegar a RegisterFragment
        binding.goToRegisterTextView.setOnClickListener {
            findNavController().navigate(R.id.action_login_to_register)
        }
    }

    /**
     * Observa el estado de login del ViewModel.
     */
    private fun observeLoginState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.loginState.collect { state ->
                    when (state) {
                        is Resource.Loading -> {
                            showLoading(true)
                        }

                        is Resource.Success -> {
                            showLoading(false)
                            navigateToHome()
                            viewModel.resetLoginState()
                        }

                        is Resource.Error -> {
                            showLoading(false)
                            showError(state.message)
                        }

                        null -> {
                            showLoading(false)
                        }
                    }
                }
            }
        }
    }

    /**
     * Muestra u oculta el indicador de carga y deshabilita el botón.
     */
    private fun showLoading(isLoading: Boolean) {
        binding.loginButton.isEnabled = !isLoading
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
    }

    /**
     * Muestra un mensaje de error en Snackbar.
     */
    private fun showError(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
    }

    /**
     * Navega a HomeActivity y cierra AuthActivity.
     */
    private fun navigateToHome() {
        val intent = Intent(requireContext(), HomeActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        requireActivity().finish()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}