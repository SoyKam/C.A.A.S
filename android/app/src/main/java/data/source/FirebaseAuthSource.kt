package com.caas.app.data.source

import com.caas.app.core.utils.Resource
import com.caas.app.domain.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.UserProfileChangeRequest
import kotlinx.coroutines.tasks.await

/**
 * Fuente de datos que encapsula Firebase Authentication.
 * Convierte operaciones de Firebase a coroutines con await().
 */
class FirebaseAuthSource {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    /**
     * Inicia sesión con email y contraseña.
     */
    suspend fun signIn(email: String, password: String): Resource<User> {
        return try {
            val result = auth.signInWithEmailAndPassword(email, password).await()
            val firebaseUser = result.user

            if (firebaseUser != null) {
                val user = User(
                    uid = firebaseUser.uid,
                    email = firebaseUser.email ?: email,
                    name = firebaseUser.displayName ?: ""
                )
                Resource.Success(user)
            } else {
                Resource.Error("No se pudo obtener la información del usuario")
            }
        } catch (e: FirebaseAuthException) {
            Resource.Error(mapFirebaseAuthError(e))
        } catch (e: Exception) {
            Resource.Error("Error al iniciar sesión: ${e.message ?: "Error desconocido"}")
        }
    }

    /**
     * Registra un nuevo usuario y actualiza su displayName.
     */
    suspend fun signUp(email: String, password: String, name: String): Resource<User> {
        return try {
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            val firebaseUser = result.user

            if (firebaseUser != null) {
                // Actualizar displayName en Firebase Auth
                val profileUpdates = UserProfileChangeRequest.Builder()
                    .setDisplayName(name)
                    .build()
                firebaseUser.updateProfile(profileUpdates).await()

                val user = User(
                    uid = firebaseUser.uid,
                    email = firebaseUser.email ?: email,
                    name = name
                )
                Resource.Success(user)
            } else {
                Resource.Error("No se pudo crear la cuenta")
            }
        } catch (e: FirebaseAuthException) {
            Resource.Error(mapFirebaseAuthError(e))
        } catch (e: Exception) {
            Resource.Error("Error al registrar usuario: ${e.message ?: "Error desconocido"}")
        }
    }

    /**
     * Cierra la sesión del usuario actual.
     */
    fun signOut() {
        auth.signOut()
    }

    /**
     * Obtiene el usuario actualmente autenticado.
     */
    fun getCurrentUser(): User? {
        val firebaseUser = auth.currentUser
        return if (firebaseUser != null) {
            User(
                uid = firebaseUser.uid,
                email = firebaseUser.email ?: "",
                name = firebaseUser.displayName ?: ""
            )
        } else {
            null
        }
    }

    /**
     * Mapea códigos de error de Firebase Auth a mensajes en español.
     */
    private fun mapFirebaseAuthError(exception: FirebaseAuthException): String {
        return when (exception.errorCode) {
            "ERROR_INVALID_EMAIL" -> "El formato del email no es válido"
            "ERROR_WRONG_PASSWORD" -> "La contraseña es incorrecta"
            "ERROR_USER_NOT_FOUND" -> "No existe una cuenta con este email"
            "ERROR_USER_DISABLED" -> "Esta cuenta ha sido deshabilitada"
            "ERROR_TOO_MANY_REQUESTS" -> "Demasiados intentos. Intenta más tarde"
            "ERROR_EMAIL_ALREADY_IN_USE" -> "Ya existe una cuenta con este email"
            "ERROR_WEAK_PASSWORD" -> "La contraseña es demasiado débil"
            "ERROR_NETWORK_REQUEST_FAILED" -> "Error de conexión. Verifica tu internet"
            else -> "Error de autenticación: ${exception.message ?: "Error desconocido"}"
        }
    }
}