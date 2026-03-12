package com.caas.app.domain.repository

import com.caas.app.core.utils.Resource
import com.caas.app.domain.model.User

/**
 * Interfaz que define el contrato para operaciones de autenticación.
 * Pertenece al dominio, sin conocimiento de implementación (Firebase).
 */
interface AuthRepository {

    /**
     * Inicia sesión con email y contraseña.
     */
    suspend fun login(email: String, password: String): Resource<User>

    /**
     * Registra un nuevo usuario y guarda su perfil.
     */
    suspend fun register(email: String, password: String, name: String): Resource<User>

    /**
     * Cierra la sesión del usuario actual.
     */
    suspend fun logout()

    /**
     * Obtiene el usuario actualmente autenticado.
     */
    fun getCurrentUser(): User?
}