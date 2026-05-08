package com.caas.app.ui.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.caas.app.databinding.FragmentProfileBinding
import com.google.firebase.auth.FirebaseAuth

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private val auth = FirebaseAuth.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupUserInfo()
        setupClickListeners()
    }

    private fun setupUserInfo() {
        val user = auth.currentUser
        // Mostramos el nombre si existe, si no "Usuario"
        binding.tvUserName.text = user?.displayName ?: "Usuario"
        // Mostramos el email si existe, si no un placeholder
        binding.tvUserEmail.text = user?.email ?: "correo@ejemplo.com"
    }

    private fun setupClickListeners() {
        // Clic en el avatar circular
        binding.avatarContainer.setOnClickListener {
            showToast("Cambiar foto de perfil")
        }

        // Clic en Editar Perfil
        binding.optionEditProfile.setOnClickListener {
            showToast("Editar perfil")
        }

        // Clic en Cambiar Contraseña
        binding.optionPassword.setOnClickListener {
            showToast("Cambiar contraseña")
        }

        // Clic en Configuración
        binding.optionSettings.setOnClickListener {
            showToast("Configuración")
        }

        // Clic en Cerrar Sesión
        binding.optionLogout.setOnClickListener {
            showToast("Sesión cerrada")
            // Aquí podrías añadir la lógica real de logout:
            // auth.signOut()
            // findNavController().navigate(R.id.auth_graph)
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
