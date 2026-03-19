package com.caas.app.domain.usecase

import com.caas.app.core.result.Result
import com.caas.app.data.model.Business
import com.caas.app.domain.repository.BusinessRepository
import com.google.firebase.auth.FirebaseAuth

/**
 * UseCase para obtener todos los negocios del usuario autenticado.
 * Consulta Firestore por ownerId == currentUser.uid
 */
class GetBusinessesByOwnerUseCase(
    private val repository: BusinessRepository,
    private val firebaseAuth: FirebaseAuth
) {

    suspend operator fun invoke(): Result<List<Business>> {
        return try {
            val currentUser = firebaseAuth.currentUser
                ?: return Result.Error("Usuario no autenticado")

            repository.getBusinessesByOwnerId(currentUser.uid)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Error al obtener negocios")
        }
    }
}