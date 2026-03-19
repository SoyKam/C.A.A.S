package com.caas.app.domain.usecase

import com.caas.app.core.result.Result
import com.caas.app.data.model.Business
import com.caas.app.domain.repository.BusinessRepository

/**
 * UseCase para obtener el detalle de un negocio por su ID.
 * Consulta Firestore directamente por businessId.
 */
class GetBusinessByIdUseCase(
    private val repository: BusinessRepository
) {

    suspend operator fun invoke(businessId: String): Result<Business> {
        return if (businessId.isBlank()) {
            Result.Error("ID de negocio vacío")
        } else {
            try {
                repository.getBusinessById(businessId)
            } catch (e: Exception) {
                Result.Error(e.message ?: "Error al obtener el negocio")
            }
        }
    }
}