package com.caas.app.ui.home

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.caas.app.R
import com.google.firebase.auth.FirebaseAuth

/**
 * Pantalla principal después del login exitoso.
 * Implementa lógica para decidir si el usuario tiene negocios o debe crearlos.
 */
class HomeActivity : AppCompatActivity() {

    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        // Configurar Navigation
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment_home) as NavHostFragment
        navController = navHostFragment.navController

        // Verificar si el usuario está autenticado
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser == null) {
            // Si no está autenticado, volver a AuthActivity
            finish()
            return
        }

        // TODO: Aquí iría lógica para verificar si tiene negocios
        // Por ahora, el nav_graph inicia en businessListFragment
        // que será responsable de verificar y decidir si navegar a createBusiness
    }
}