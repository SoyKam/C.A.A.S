package com.caas.app.ui.auth

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.caas.app.databinding.ActivityAuthBinding
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions

/**
 * Activity contenedora del flujo de autenticación.
 * Solo hospeda el NavHostFragment para navegación entre LoginFragment y RegisterFragment.
 */
class AuthActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAuthBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inicialización manual de Firebase para saltar la necesidad del archivo google-services.json
        val options = FirebaseOptions.Builder()
            .setProjectId("caas-8443d")
            .setApplicationId("1:134353714726:android:70f95566ba847010eef0e7")
            .setApiKey("AIzaSyAGvIrGAaw48Ixx3i4T0PYZ0yWFbFBirs4")
            .setStorageBucket("caas-8443d.firebasestorage.app")
            .build()

        if (FirebaseApp.getApps(this).isEmpty()) {
            FirebaseApp.initializeApp(this, options)
        }

        binding = ActivityAuthBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Navigation Component se configura automáticamente desde el layout
    }
}