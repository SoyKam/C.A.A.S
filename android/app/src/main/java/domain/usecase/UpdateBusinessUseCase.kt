package com.caas.app.domain.usecase

import com.caas.app.core.result.Result
import com.caas.app.data.model.Business
import com.caas.app.domain.repository.BusinessRepository

/**
 * UseCase para actualizar la información de un negocio existente.
 * Implementa RF-06: Editar información del negocio
 */
class UpdateBusinessUseCase(
    private val businessRepository: BusinessRepository
) {

    suspend operator fun invoke(
        businessId: String,
        name: String,
        sector: String,
        taxId: String
    ): Result<Business> {
        // Validación de campos vacíos
        if (businessId.isBlank()) {
            return Result.Error("El ID del negocio es requerido")
        }
        if (name.isBlank()) {
            return Result.Error("El nombre del negocio es requerido")
        }
        if (sector.isBlank()) {
            return Result.Error("El sector del negocio es requerido")
        }
        if (taxId.isBlank()) {
            return Result.Error("La identificación fiscal es requerida")
        }

        // Crear objeto Business con datos actualizados
        val business = Business(
            id = businessId,
            name = name.trim(),
            sector = sector.trim(),
            taxId = taxId.trim()
        )

        return businessRepository.updateBusiness(business)
    }
}