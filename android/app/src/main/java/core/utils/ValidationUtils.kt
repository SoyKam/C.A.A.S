package com.caas.app.core.utils

import android.util.Patterns

/**
 Utilidades de validación para inputs del usuario
 Usado principalmente en AuthViewModel antes de llamar al repositorio.
 */
object ValidationUtils {

    /**
     * Valida que el email tenga formato correcto.
     */
    fun isValidEmail(email: String): Boolean {
        return email.isNotBlank() && Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    /**
     * Valida que la contraseña tenga al menos 6 caracteres (requisito de Firebase Auth).
     */
    fun isValidPassword(password: String): Boolean {
        return password.length >= 6
    }

    /**
     * Valida que el nombre no esté vacío ni sea solo espacios en blanco.
     */
    fun isValidName(name: String): Boolean {
        return name.trim().isNotBlank()
    }
}