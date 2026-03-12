package com.caas.app.data.repository

import com.caas.app.core.utils.Resource
import com.caas.app.data.source.FirebaseAuthSource
import com.caas.app.domain.model.User
import com.caas.app.domain.repository.AuthRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

/**
 * Implementación del repositorio de autenticación.
 * Coordina Firebase Auth y Firestore para login/registro.
 */
class AuthRepositoryImpl(
    private val authSource: FirebaseAuthSource
) : AuthRepository {

    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()

    /**
     * Inicia sesión con email y contraseña.
     */
    override suspend fun login(email: String, password: String): Resource<User> {
        return authSource.signIn(email, password)
    }

    /**
     * Registra un nuevo usuario y guarda su perfil en Firestore.
     */
    override suspend fun register(email: String, password: String, name: String): Resource<User> {
        return try {
            // Paso 1: Crear cuenta en Firebase Auth
            val authResult = authSource.signUp(email, password, name)

            if (authResult is Resource.Success) {
                val user = authResult.data

                // Paso 2: Guardar perfil en Firestore colección "users"
                try {
                    val userDoc = hashMapOf(
                        "uid" to user.uid,
                        "email" to user.email,
                        "name" to user.name
                    )
                    firestore.collection("users")
                        .document(user.uid)
                        .set(userDoc)
                        .await()

                    Resource.Success(user)
                } catch (e: Exception) {
                    Resource.Error("Error al guardar el perfil: ${e.message ?: "Error desconocido"}")
                }
            } else if (authResult is Resource.Error) {
                authResult
            } else {
                Resource.Error("Error inesperado durante el registro")
            }
        } catch (e: Exception) {
            Resource.Error("Error al registrar usuario: ${e.message ?: "Error desconocido"}")
        }
    }

    /**
     * Cierra la sesión del usuario actual.
     */
    override suspend fun logout() {
        authSource.signOut()
    }

    /**
     * Obtiene el usuario actualmente autenticado.
     */
    override fun getCurrentUser(): User? {
        return authSource.getCurrentUser()
    }
}