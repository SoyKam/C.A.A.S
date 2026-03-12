package com.caas.app.ui.home

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.caas.app.R

/**
 * Pantalla principal después del login exitoso.
 * TODO: Implementar funcionalidad del almacén e inventario.
 */
class HomeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
    }
}