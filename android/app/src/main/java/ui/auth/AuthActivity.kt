package com.caas.app.ui.auth

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.caas.app.databinding.ActivityAuthBinding

/**
 * Activity contenedora del flujo de autenticación.
 * Solo hospeda el NavHostFragment para navegación entre LoginFragment y RegisterFragment.
 */
class AuthActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAuthBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAuthBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Navigation Component se configura automáticamente desde el layout
    }
}