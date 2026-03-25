package com.caas.app.domain.usecase

import com.caas.app.core.result.Result
import com.caas.app.domain.repository.BranchRepository

class DeleteBranchUseCase(
    private val branchRepository: BranchRepository
) {

    suspend operator fun invoke(businessId: String, branchId: String): Result<Unit> {
        if (businessId.isBlank()) {
            return Result.Error("El ID del negocio es requerido")
        }
        if (branchId.isBlank()) {
            return Result.Error("El ID de la sucursal es requerido")
        }
        return branchRepository.deleteBranch(businessId, branchId)
    }
}
