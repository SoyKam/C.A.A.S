package com.caas.app.domain.usecase

import com.caas.app.core.result.Result
import com.caas.app.data.model.Branch
import com.caas.app.domain.repository.BranchRepository

class CreateBranchUseCase(
    private val branchRepository: BranchRepository
) {

    suspend operator fun invoke(
        businessId: String,
        name: String,
        address: String,
        phone: String
    ): Result<Branch> {
        if (businessId.isBlank()) {
            return Result.Error("El ID del negocio es requerido")
        }
        if (name.isBlank()) {
            return Result.Error("El nombre de la sucursal es requerido")
        }
        if (address.isBlank()) {
            return Result.Error("La dirección de la sucursal es requerida")
        }
        if (phone.isBlank()) {
            return Result.Error("El teléfono de la sucursal es requerido")
        }

        val now = System.currentTimeMillis()
        val branchId = "${businessId}_${System.nanoTime()}"

        val branch = Branch(
            id = branchId,
            businessId = businessId,
            name = name.trim(),
            address = address.trim(),
            phone = phone.trim(),
            isActive = true,
            createdAt = now,
            updatedAt = now
        )

        return branchRepository.createBranch(branch)
    }
}
