package com.caas.app.domain.usecase

import com.caas.app.core.result.Result
import com.caas.app.data.model.Branch
import com.caas.app.domain.repository.BranchRepository

class GetBranchesByBusinessUseCase(
    private val branchRepository: BranchRepository
) {

    suspend operator fun invoke(businessId: String): Result<List<Branch>> {
        if (businessId.isBlank()) {
            return Result.Error("El ID del negocio es requerido")
        }
        return try {
            branchRepository.getBranchesByBusinessId(businessId)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Error al obtener las sucursales")
        }
    }
}
