package com.caas.app.domain.usecase

import com.caas.app.core.result.Result
import com.caas.app.data.model.Business
import com.caas.app.data.model.BusinessMember
import com.caas.app.data.model.UserRole
import com.caas.app.domain.repository.BusinessRepository
import com.google.firebase.auth.FirebaseAuth

/**
 * UseCase para crear un nuevo negocio.
 * Implementa RF-05: Crear negocio
 * Implementa RF-07: Asociar contenido al negocio autenticado
 */
class CreateBusinessUseCase(
    private val businessRepository: BusinessRepository,
    private val firebaseAuth: FirebaseAuth
) {

    suspend operator fun invoke(
        name: String,
        sector: String,
        taxId: String
    ): Result<Business> {
        // Validación de campos vacíos (RF-05)
        if (name.isBlank()) {
            return Result.Error("El nombre del negocio es requerido")
        }
        if (sector.isBlank()) {
            return Result.Error("El sector del negocio es requerido")
        }
        if (taxId.isBlank()) {
            return Result.Error("La identificación fiscal es requerida")
        }

        val currentUser = firebaseAuth.currentUser
            ?: return Result.Error("No hay un usuario autenticado")

        // Validar máximo 3 negocios
        val businessesResult = businessRepository.getBusinessesByOwnerId(currentUser.uid)
        if (businessesResult is Result.Success) {
            if (businessesResult.data.size >= 3) {
                return Result.Error("No puedes tener más de 3 negocios. Elimina uno para crear otro.")
            }
        }

        val now = System.currentTimeMillis()
        // ID único: uid_timestamp para cada negocio diferente
        val businessId = "${currentUser.uid}_${System.nanoTime()}"

        val business = Business(
            id = businessId,
            ownerId = currentUser.uid,
            name = name.trim(),
            sector = sector.trim(),
            taxId = taxId.trim(),
            isActive = true,
            createdAt = now,
            updatedAt = now
        )

        val ownerMember = BusinessMember(
            userId = currentUser.uid,
            businessId = businessId,
            email = currentUser.email.orEmpty(),
            displayName = currentUser.displayName.orEmpty(),
            role = UserRole.OWNER,
            branchId = null,
            isActive = true,
            invitedAt = now,
            joinedAt = now,
            createdAt = now,
            updatedAt = now
        )

        return businessRepository.createBusinessWithOwner(business, ownerMember)
    }
}