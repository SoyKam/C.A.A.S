package com.caas.app.ui.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.caas.app.databinding.FragmentSettingsBinding

class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupSettings()
    }

    private fun setupSettings() {
        // Modo Oscuro (Simulado)
        binding.switchDarkMode.setOnCheckedChangeListener { _, isChecked ->
            val message = if (isChecked) "Modo oscuro activado" else "Modo oscuro desactivado"
            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
        }

        // Notificaciones (Simulado)
        binding.switchNotifications.setOnCheckedChangeListener { _, isChecked ->
            val message = if (isChecked) "Notificaciones habilitadas" else "Notificaciones silenciadas"
            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
        }

        // Versión de la app (Hardcoded por ahora)
        binding.tvAppVersion.text = "1.0.0 (Build 2024)"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
