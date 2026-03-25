package com.caas.app.domain.usecase

import com.caas.app.core.result.Result
import com.caas.app.data.model.Branch
import com.caas.app.domain.repository.BranchRepository

class GetBranchByIdUseCase(
    private val branchRepository: BranchRepository
) {

    suspend operator fun invoke(businessId: String, branchId: String): Result<Branch> {
        if (businessId.isBlank()) {
            return Result.Error("El ID del negocio es requerido")
        }
        if (branchId.isBlank()) {
            return Result.Error("El ID de la sucursal es requerido")
        }
        return try {
            branchRepository.getBranchById(businessId, branchId)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Error al obtener la sucursal")
        }
    }
}
