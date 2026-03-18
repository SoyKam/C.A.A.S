package com.caas.app.core.utils

import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.tasks.await

/**
 * Helper para extraer el businessId del usuario autenticado desde Firestore.
 * Reutilizable en todos los UseCases que necesiten asociar contenido al negocio.
 */
class GetAuthenticatedUserBusinessIdHelper(
    private val firebaseAuth: FirebaseAuth
) {

    /**
     * Obtiene el UID del usuario autenticado actual.
     * @return UID del usuario autenticado, o null si no hay usuario en sesión.
     */
    fun getCurrentUserId(): String? = firebaseAuth.currentUser?.uid

    /**
     * Valida que exista usuario autenticado.
     * @return true si hay usuario autenticado, false en caso contrario.
     */
    fun isUserAuthenticated(): Boolean = firebaseAuth.currentUser != null
}